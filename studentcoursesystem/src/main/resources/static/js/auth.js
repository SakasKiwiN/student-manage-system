// 用户认证管理
const AuthManager = {
    // 当前用户信息
    currentUser: null,

    // 用户角色定义
    ROLES: {
        SYS_ADMIN: 'SYS_ADMIN',
        COLLEGE_ADMIN: 'COLLEGE_ADMIN',
        TEACHER: 'TEACHER',
        STUDENT: 'STUDENT'
    },

    // 角色显示名称
    ROLE_NAMES: {
        'SYS_ADMIN': '系统管理员',
        'COLLEGE_ADMIN': '学院管理员',
        'TEACHER': '教师',
        'STUDENT': '学生'
    },

    // 初始化认证状态
    async init() {
        try {
            const response = await API.Auth.checkStatus();
            if (response.code === 200) {
                // 已登录，获取用户详细信息
                await this.loadCurrentUser();
                return true;
            }
        } catch (error) {
            console.log('用户未登录或登录已过期');
        }
        return false;
    },

    // 登录
    async login(username, password) {
        try {
            const response = await API.Auth.login(username, password);
            if (response.code === 200) {
                await this.loadCurrentUser();
                return { success: true, data: response.data };
            } else {
                return { success: false, message: response.message };
            }
        } catch (error) {
            return { success: false, message: error.message };
        }
    },

    // 登出
    async logout() {
        try {
            await API.Auth.logout();
        } catch (error) {
            console.warn('Logout request failed:', error);
        } finally {
            this.currentUser = null;
            StorageUtils.removeLocal('currentUser');
            window.location.href = '/index.html';
        }
    },

    // 加载当前用户信息
    async loadCurrentUser() {
        try {
            const response = await API.Auth.getCurrentUser();
            if (response.code === 200) {
                this.currentUser = response.data;
                StorageUtils.setLocal('currentUser', this.currentUser);
                return this.currentUser;
            }
        } catch (error) {
            console.error('Failed to load current user:', error);
            this.currentUser = null;
            StorageUtils.removeLocal('currentUser');
        }
        return null;
    },

    // 获取当前用户
    getCurrentUser() {
        if (!this.currentUser) {
            this.currentUser = StorageUtils.getLocal('currentUser');
        }
        return this.currentUser;
    },

    // 检查是否已登录
    isAuthenticated() {
        return !!this.getCurrentUser();
    },

    // 获取用户角色
    getUserRoles() {
        const user = this.getCurrentUser();
        if (!user || !user.userRoles) return [];

        return user.userRoles.map(ur => ur.role.name);
    },

    // 检查是否有指定角色
    hasRole(roleName) {
        return this.getUserRoles().includes(roleName);
    },

    // 检查是否有任意指定角色
    hasAnyRole(roleNames) {
        const userRoles = this.getUserRoles();
        return roleNames.some(role => userRoles.includes(role));
    },

    // 检查是否为系统管理员
    isSysAdmin() {
        return this.hasRole(this.ROLES.SYS_ADMIN);
    },

    // 检查是否为学院管理员
    isCollegeAdmin() {
        return this.hasRole(this.ROLES.COLLEGE_ADMIN);
    },

    // 检查是否为教师
    isTeacher() {
        return this.hasRole(this.ROLES.TEACHER);
    },

    // 检查是否为学生
    isStudent() {
        return this.hasRole(this.ROLES.STUDENT);
    },

    // 获取主要角色（最高权限角色）
    getPrimaryRole() {
        if (this.isSysAdmin()) return this.ROLES.SYS_ADMIN;
        if (this.isCollegeAdmin()) return this.ROLES.COLLEGE_ADMIN;
        if (this.isTeacher()) return this.ROLES.TEACHER;
        if (this.isStudent()) return this.ROLES.STUDENT;
        return null;
    },

    // 获取角色显示名称
    getRoleDisplayName() {
        const primaryRole = this.getPrimaryRole();
        return this.ROLE_NAMES[primaryRole] || '未知角色';
    },

    // 获取用户学院ID（针对学院管理员、教师、学生）
    getUserCollegeId() {
        const user = this.getCurrentUser();
        if (!user) return null;

        // 通过角色获取关联的学院ID
        if (this.isStudent() && user.student) {
            return user.student.college?.id;
        }
        if ((this.isTeacher() || this.isCollegeAdmin()) && user.teacher) {
            return user.teacher.college?.id;
        }
        return null;
    },

    // 权限检查
    checkPermission(resource, action) {
        const role = this.getPrimaryRole();
        if (!role) return false;

        // 权限映射表
        const permissions = {
            [this.ROLES.SYS_ADMIN]: {
                '*': ['*'] // 系统管理员拥有所有权限
            },
            [this.ROLES.COLLEGE_ADMIN]: {
                'college': ['view', 'edit'],
                'course': ['view', 'create', 'edit', 'delete'],
                'teacher': ['view', 'create', 'edit', 'delete'],
                'student': ['view', 'edit'],
                'score': ['view', 'edit'],
                'courseSelection': ['view', 'lottery']
            },
            [this.ROLES.TEACHER]: {
                'course': ['view', 'edit'],
                'courseware': ['view', 'upload', 'edit', 'delete'],
                'score': ['view', 'input', 'edit'],
                'student': ['view'],
                'message': ['send', 'receive']
            },
            [this.ROLES.STUDENT]: {
                'course': ['view'],
                'courseSelection': ['create', 'cancel'],
                'courseware': ['view', 'download'],
                'score': ['view'],
                'message': ['send', 'receive']
            }
        };

        const rolePermissions = permissions[role];
        if (!rolePermissions) return false;

        // 检查通配符权限
        if (rolePermissions['*'] && rolePermissions['*'].includes('*')) {
            return true;
        }

        // 检查具体资源权限
        const resourcePermissions = rolePermissions[resource];
        if (!resourcePermissions) return false;

        return resourcePermissions.includes(action) || resourcePermissions.includes('*');
    }
};

// 页面权限控制
const PageAuth = {
    // 需要登录的页面
    protectedPages: [
        '/main.html',
        '/pages/admin/',
        '/pages/college/',
        '/pages/teacher/',
        '/pages/student/',
        '/pages/common/'
    ],

    // 角色页面映射
    rolePages: {
        [AuthManager.ROLES.SYS_ADMIN]: [
            '/pages/admin/',
            '/pages/common/'
        ],
        [AuthManager.ROLES.COLLEGE_ADMIN]: [
            '/pages/college/',
            '/pages/common/'
        ],
        [AuthManager.ROLES.TEACHER]: [
            '/pages/teacher/',
            '/pages/common/'
        ],
        [AuthManager.ROLES.STUDENT]: [
            '/pages/student/',
            '/pages/common/'
        ]
    },

    // 检查页面访问权限
    checkPageAccess(pathname) {
        // 检查是否需要登录
        const needsAuth = this.protectedPages.some(path => pathname.includes(path));

        if (needsAuth && !AuthManager.isAuthenticated()) {
            return { allowed: false, redirect: '/index.html' };
        }

        // 检查角色权限
        if (needsAuth) {
            const userRoles = AuthManager.getUserRoles();
            const hasAccess = userRoles.some(role => {
                const allowedPages = this.rolePages[role] || [];
                return allowedPages.some(page => pathname.includes(page));
            });

            if (!hasAccess) {
                return { allowed: false, redirect: '/main.html' };
            }
        }

        return { allowed: true };
    },

    // 重定向到适当的页面
    redirectToRolePage() {
        const primaryRole = AuthManager.getPrimaryRole();

        switch (primaryRole) {
            case AuthManager.ROLES.SYS_ADMIN:
                window.location.href = '/pages/admin/college-manage.html';
                break;
            case AuthManager.ROLES.COLLEGE_ADMIN:
                window.location.href = '/pages/college/course-manage.html';
                break;
            case AuthManager.ROLES.TEACHER:
                window.location.href = '/pages/teacher/course-list.html';
                break;
            case AuthManager.ROLES.STUDENT:
                window.location.href = '/pages/student/course-select.html';
                break;
            default:
                window.location.href = '/main.html';
        }
    }
};

// 菜单配置
const MenuConfig = {
    // 根据角色获取菜单
    getMenusByRole(role) {
        const menus = {
            [AuthManager.ROLES.SYS_ADMIN]: [
                {
                    title: '系统管理',
                    icon: '⚙️',
                    children: [
                        { title: '学院管理', url: '/pages/admin/college-manage.html' },
                        { title: '学生管理', url: '/pages/admin/student-manage.html' },
                        { title: '教师管理', url: '/pages/admin/teacher-manage.html' }
                    ]
                },
                {
                    title: '个人中心',
                    icon: '👤',
                    children: [
                        { title: '个人信息', url: '/pages/common/profile.html' },
                        { title: '修改密码', url: '/pages/common/change-password.html' },
                        { title: '消息中心', url: '/pages/common/messages.html' }
                    ]
                }
            ],
            [AuthManager.ROLES.COLLEGE_ADMIN]: [
                {
                    title: '学院管理',
                    icon: '🏫',
                    children: [
                        { title: '课程管理', url: '/pages/college/course-manage.html' },
                        { title: '学生列表', url: '/pages/college/student-list.html' },
                        { title: '抽签管理', url: '/pages/college/lottery.html' }
                    ]
                },
                {
                    title: '个人中心',
                    icon: '👤',
                    children: [
                        { title: '个人信息', url: '/pages/common/profile.html' },
                        { title: '修改密码', url: '/pages/common/change-password.html' },
                        { title: '消息中心', url: '/pages/common/messages.html' }
                    ]
                }
            ],
            [AuthManager.ROLES.TEACHER]: [
                {
                    title: '教学管理',
                    icon: '📚',
                    children: [
                        { title: '我的课程', url: '/pages/teacher/course-list.html' },
                        { title: '成绩录入', url: '/pages/teacher/score-input.html' },
                        { title: '课件管理', url: '/pages/teacher/courseware.html' },
                        { title: '学生列表', url: '/pages/teacher/student-list.html' }
                    ]
                },
                {
                    title: '个人中心',
                    icon: '👤',
                    children: [
                        { title: '个人信息', url: '/pages/common/profile.html' },
                        { title: '修改密码', url: '/pages/common/change-password.html' },
                        { title: '消息中心', url: '/pages/common/messages.html' }
                    ]
                }
            ],
            [AuthManager.ROLES.STUDENT]: [
                {
                    title: '选课系统',
                    icon: '📖',
                    children: [
                        { title: '课程选择', url: '/pages/student/course-select.html' },
                        { title: '我的课程', url: '/pages/student/my-courses.html' },
                        { title: '成绩查询', url: '/pages/student/scores.html' },
                        { title: '课件下载', url: '/pages/student/courseware.html' }
                    ]
                },
                {
                    title: '个人中心',
                    icon: '👤',
                    children: [
                        { title: '个人信息', url: '/pages/common/profile.html' },
                        { title: '修改密码', url: '/pages/common/change-password.html' },
                        { title: '消息中心', url: '/pages/common/messages.html' }
                    ]
                }
            ]
        };

        return menus[role] || [];
    }
};

// 页面初始化时的认证检查
document.addEventListener('DOMContentLoaded', async function() {
    const pathname = window.location.pathname;

    // 如果是登录页面，检查是否已登录
    if (pathname === '/index.html' || pathname === '/') {
        if (await AuthManager.init()) {
            // 已登录，重定向到主页
            window.location.href = '/main.html';
            return;
        }
        return; // 未登录，停留在登录页
    }

    // 检查页面访问权限
    const authResult = PageAuth.checkPageAccess(pathname);
    if (!authResult.allowed) {
        window.location.href = authResult.redirect;
        return;
    }

    // 初始化用户认证状态
    const isAuthenticated = await AuthManager.init();
    if (!isAuthenticated) {
        window.location.href = '/index.html';
        return;
    }

    // 触发自定义事件，通知页面用户已认证
    window.dispatchEvent(new CustomEvent('userAuthenticated', {
        detail: { user: AuthManager.getCurrentUser() }
    }));
});

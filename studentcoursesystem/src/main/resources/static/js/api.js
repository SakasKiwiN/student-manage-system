// API基础配置
const API_CONFIG = {
    baseURL: '',  // 使用相对路径，因为前后端在同一服务器
    timeout: 30000,
    headers: {
        'Content-Type': 'application/json',
        'X-Requested-With': 'XMLHttpRequest'
    }
};

// HTTP请求封装
const HttpClient = {
    // 发送请求的基础方法
    async request(url, options = {}) {
        const config = {
            credentials: 'include', // 包含cookie，用于Session认证
            headers: { ...API_CONFIG.headers, ...options.headers },
            ...options
        };

        try {
            const response = await fetch(API_CONFIG.baseURL + url, config);

            // 处理HTTP状态码
            if (response.status === 401) {
                // 未授权，跳转到登录页
                Utils.showMessage('登录已过期，请重新登录', 'warning');
                setTimeout(() => {
                    window.location.href = '/index.html';
                }, 1500);
                throw new Error('Unauthorized');
            }

            if (response.status === 403) {
                Utils.showMessage('权限不足，无法访问该资源', 'danger');
                throw new Error('Forbidden');
            }

            if (response.status === 404) {
                Utils.showMessage('请求的资源不存在', 'warning');
                throw new Error('Not Found');
            }

            if (response.status >= 500) {
                Utils.showMessage('服务器内部错误，请稍后重试', 'danger');
                throw new Error('Server Error');
            }

            // 尝试解析JSON响应
            const data = await response.json();

            // 处理业务逻辑返回的错误
            if (data.code !== 200 && data.code !== undefined) {
                throw new Error(data.message || '请求失败');
            }

            return data;
        } catch (error) {
            if (error.name === 'TypeError' && error.message.includes('fetch')) {
                Utils.showMessage('网络连接失败，请检查网络设置', 'danger');
            } else if (error.message !== 'Unauthorized' && error.message !== 'Forbidden') {
                Utils.showMessage(error.message || '请求失败', 'danger');
            }
            throw error;
        }
    },

    // GET请求
    get(url, params = {}) {
        const queryString = new URLSearchParams(params).toString();
        const fullUrl = queryString ? `${url}?${queryString}` : url;
        return this.request(fullUrl, { method: 'GET' });
    },

    // POST请求
    post(url, data = {}) {
        return this.request(url, {
            method: 'POST',
            body: JSON.stringify(data)
        });
    },

    // PUT请求
    put(url, data = {}) {
        return this.request(url, {
            method: 'PUT',
            body: JSON.stringify(data)
        });
    },

    // DELETE请求
    delete(url) {
        return this.request(url, { method: 'DELETE' });
    },

    // 文件上传
    upload(url, formData) {
        return this.request(url, {
            method: 'POST',
            body: formData,
            headers: {} // 让浏览器自动设置Content-Type
        });
    },

    // 文件下载
    async download(url, filename) {
        try {
            const response = await fetch(API_CONFIG.baseURL + url, {
                credentials: 'include'
            });

            if (!response.ok) {
                throw new Error('下载失败');
            }

            const blob = await response.blob();
            const downloadUrl = window.URL.createObjectURL(blob);
            const a = document.createElement('a');
            a.href = downloadUrl;
            a.download = filename || 'download';
            document.body.appendChild(a);
            a.click();
            document.body.removeChild(a);
            window.URL.revokeObjectURL(downloadUrl);
        } catch (error) {
            Utils.showMessage('文件下载失败: ' + error.message, 'danger');
            throw error;
        }
    }
};

// 认证API
const AuthAPI = {
    // 登录
    login(username, password) {
        const formData = new URLSearchParams();
        formData.append('username', username);
        formData.append('password', password);

        return HttpClient.request('/api/auth/login', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded'
            },
            body: formData
        });
    },

    // 登出
    logout() {
        return HttpClient.post('/api/auth/logout');
    },

    // 获取当前用户信息
    getCurrentUser() {
        return HttpClient.get('/api/auth/current');
    },

    // 检查登录状态
    checkStatus() {
        return HttpClient.get('/api/auth/status');
    },

    // 修改密码
    changePassword(oldPassword, newPassword, confirmPassword) {
        return HttpClient.post('/api/auth/change-password', {
            oldPassword,
            newPassword,
            confirmPassword
        });
    }
};

// 学院API
const CollegeAPI = {
    // 获取所有学院
    getAll() {
        return HttpClient.get('/api/colleges');
    },

    // 根据ID获取学院
    getById(id) {
        return HttpClient.get(`/api/colleges/${id}`);
    },

    // 根据编码获取学院
    getByCode(code) {
        return HttpClient.get(`/api/colleges/code/${code}`);
    },

    // 创建学院
    create(college) {
        return HttpClient.post('/api/colleges', college);
    },

    // 更新学院
    update(id, college) {
        return HttpClient.put(`/api/colleges/${id}`, college);
    },

    // 删除学院
    delete(id) {
        return HttpClient.delete(`/api/colleges/${id}`);
    }
};

// 课程API
const CourseAPI = {
    // 获取所有课程
    getAll() {
        return HttpClient.get('/api/courses');
    },

    // 获取启用的课程
    getEnabled() {
        return HttpClient.get('/api/courses/enabled');
    },

    // 根据ID获取课程
    getById(id) {
        return HttpClient.get(`/api/courses/${id}`);
    },

    // 根据课程编码获取课程
    getByCode(courseCode) {
        return HttpClient.get(`/api/courses/code/${courseCode}`);
    },

    // 根据教师获取课程
    getByTeacher(teacherId) {
        return HttpClient.get(`/api/courses/teacher/${teacherId}`);
    },

    // 根据学院获取课程
    getByCollege(collegeId) {
        return HttpClient.get(`/api/courses/college/${collegeId}`);
    },

    // 创建课程
    create(course) {
        return HttpClient.post('/api/courses', course);
    },

    // 更新课程
    update(id, course) {
        return HttpClient.put(`/api/courses/${id}`, course);
    },

    // 删除课程
    delete(id) {
        return HttpClient.delete(`/api/courses/${id}`);
    },

    // 设置先修课程
    setPrerequisites(id, prerequisiteIds) {
        return HttpClient.post(`/api/courses/${id}/prerequisites`, {
            prerequisiteIds
        });
    },

    // 检查先修条件
    checkPrerequisites(id) {
        return HttpClient.get(`/api/courses/${id}/check-prerequisites`);
    }
};

// 选课API
const CourseSelectionAPI = {
    // 学生选课
    selectCourse(courseId) {
        return HttpClient.post('/api/course-selections', { courseId });
    },

    // 取消选课
    cancelSelection(courseId) {
        return HttpClient.delete(`/api/course-selections/course/${courseId}`);
    },

    // 获取我的选课
    getMySelections() {
        return HttpClient.get('/api/course-selections/my');
    },

    // 获取课程选课学生
    getCourseSelections(courseId) {
        return HttpClient.get(`/api/course-selections/course/${courseId}`);
    },

    // 获取课程中签学生
    getSelectedStudents(courseId) {
        return HttpClient.get(`/api/course-selections/course/${courseId}/selected`);
    },

    // 执行课程抽签
    performLottery(courseId) {
        return HttpClient.post(`/api/course-selections/course/${courseId}/lottery`);
    },

    // 批量抽签（学院）
    performCollegeLottery(collegeId) {
        return HttpClient.post(`/api/course-selections/college/${collegeId}/lottery`);
    },

    // 获取选课统计
    getStatistics() {
        return HttpClient.get('/api/course-selections/statistics');
    }
};

// 课件API
const CoursewareAPI = {
    // 上传课件
    upload(courseId, file, title) {
        const formData = new FormData();
        formData.append('courseId', courseId);
        formData.append('file', file);
        formData.append('title', title);
        return HttpClient.upload('/api/coursewares/upload', formData);
    },

    // 获取课程课件
    getByCourse(courseId) {
        return HttpClient.get(`/api/coursewares/course/${courseId}`);
    },

    // 获取课件详情
    getById(id) {
        return HttpClient.get(`/api/coursewares/${id}`);
    },

    // 下载课件
    download(id, filename) {
        return HttpClient.download(`/api/coursewares/${id}/download`, filename);
    },

    // 更新课件
    update(id, courseware) {
        return HttpClient.put(`/api/coursewares/${id}`, courseware);
    },

    // 删除课件
    delete(id) {
        return HttpClient.delete(`/api/coursewares/${id}`);
    }
};

// 消息API
const MessageAPI = {
    // 发送消息
    send(receiverId, content, courseId = null) {
        return HttpClient.post('/api/messages', {
            receiverId,
            content,
            courseId
        });
    },

    // 获取发送的消息
    getSent() {
        return HttpClient.get('/api/messages/sent');
    },

    // 获取接收的消息
    getReceived() {
        return HttpClient.get('/api/messages/received');
    },

    // 获取未读消息
    getUnread() {
        return HttpClient.get('/api/messages/unread');
    },

    // 获取未读消息数量
    getUnreadCount() {
        return HttpClient.get('/api/messages/unread/count');
    },

    // 标记为已读
    markAsRead(id) {
        return HttpClient.put(`/api/messages/${id}/read`);
    },

    // 标记所有为已读
    markAllAsRead() {
        return HttpClient.put('/api/messages/read-all');
    },

    // 获取课程消息
    getByCourse(courseId) {
        return HttpClient.get(`/api/messages/course/${courseId}`);
    },

    // 删除消息
    delete(id) {
        return HttpClient.delete(`/api/messages/${id}`);
    }
};

// 成绩API
const ScoreAPI = {
    // 录入成绩
    input(studentId, courseId, score) {
        return HttpClient.post('/api/scores', {
            studentId,
            courseId,
            score
        });
    },

    // 批量录入成绩
    batchInput(scores) {
        return HttpClient.post('/api/scores/batch', scores);
    },

    // 更新成绩
    update(id, score) {
        return HttpClient.put(`/api/scores/${id}`, null, { score });
    },

    // 获取我的成绩
    getMy() {
        return HttpClient.get('/api/scores/my');
    },

    // 获取学生成绩
    getByStudent(studentId) {
        return HttpClient.get(`/api/scores/student/${studentId}`);
    },

    // 获取课程成绩
    getByCourse(courseId) {
        return HttpClient.get(`/api/scores/course/${courseId}`);
    },

    // 获取课程成绩统计
    getCourseStatistics(courseId) {
        return HttpClient.get(`/api/scores/course/${courseId}/statistics`);
    },

    // 获取成绩分布
    getDistribution(courseId) {
        return HttpClient.get(`/api/scores/course/${courseId}/distribution`);
    },

    // 删除成绩
    delete(id) {
        return HttpClient.delete(`/api/scores/${id}`);
    }
};

// 学生API
const StudentAPI = {
    // 获取所有学生
    getAll() {
        return HttpClient.get('/api/students');
    },

    // 根据ID获取学生
    getById(id) {
        return HttpClient.get(`/api/students/${id}`);
    },

    // 根据学号获取学生
    getByNo(studentNo) {
        return HttpClient.get(`/api/students/no/${studentNo}`);
    },

    // 获取当前学生信息
    getCurrent() {
        return HttpClient.get('/api/students/current');
    },

    // 根据学院获取学生
    getByCollege(collegeId) {
        return HttpClient.get(`/api/students/college/${collegeId}`);
    },

    // 创建学生
    create(student) {
        return HttpClient.post('/api/students', student);
    },

    // 更新学生
    update(id, student) {
        return HttpClient.put(`/api/students/${id}`, student);
    },

    // 删除学生
    delete(id) {
        return HttpClient.delete(`/api/students/${id}`);
    },

    // 批量创建学生
    batchCreate(students) {
        return HttpClient.post('/api/students/batch', students);
    }
};

// 教师API
const TeacherAPI = {
    // 获取所有教师
    getAll() {
        return HttpClient.get('/api/teachers');
    },

    // 根据ID获取教师
    getById(id) {
        return HttpClient.get(`/api/teachers/${id}`);
    },

    // 获取当前教师信息
    getCurrent() {
        return HttpClient.get('/api/teachers/current');
    },

    // 根据学院获取教师
    getByCollege(collegeId) {
        return HttpClient.get(`/api/teachers/college/${collegeId}`);
    },

    // 创建教师
    create(teacher) {
        return HttpClient.post('/api/teachers', teacher);
    },

    // 更新教师
    update(id, teacher) {
        return HttpClient.put(`/api/teachers/${id}`, teacher);
    },

    // 删除教师
    delete(id) {
        return HttpClient.delete(`/api/teachers/${id}`);
    }
};

// 用户列表API（TestController中的接口）
const UserAPI = {
    // 获取用户列表
    getList() {
        return HttpClient.get('/user/list');
    },

    // 获取用户信息
    getById(id) {
        return HttpClient.get(`/user/view/${id}`);
    }
};

// 导出所有API
window.API = {
    Auth: AuthAPI,
    College: CollegeAPI,
    Course: CourseAPI,
    CourseSelection: CourseSelectionAPI,
    Courseware: CoursewareAPI,
    Message: MessageAPI,
    Score: ScoreAPI,
    Student: StudentAPI,
    Teacher: TeacherAPI,
    User: UserAPI
};

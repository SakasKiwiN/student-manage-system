// 公共工具函数
const Utils = {
    // 显示消息提示
    showMessage: function(message, type = 'info', duration = 3000) {
        // 移除已存在的消息
        const existingAlert = document.querySelector('.alert-message');
        if (existingAlert) {
            existingAlert.remove();
        }

        // 创建消息元素
        const alertDiv = document.createElement('div');
        alertDiv.className = `alert alert-${type} alert-message`;
        alertDiv.style.cssText = `
            position: fixed;
            top: 20px;
            right: 20px;
            z-index: 9999;
            min-width: 300px;
            animation: slideIn 0.3s ease-out;
        `;
        alertDiv.innerHTML = `
            <span>${message}</span>
            <button type="button" class="btn-close" onclick="this.parentElement.remove()">&times;</button>
        `;

        // 添加样式
        const style = document.createElement('style');
        style.textContent = `
            @keyframes slideIn {
                from { transform: translateX(100%); opacity: 0; }
                to { transform: translateX(0); opacity: 1; }
            }
            .btn-close {
                background: none;
                border: none;
                float: right;
                font-size: 18px;
                font-weight: bold;
                cursor: pointer;
                margin-left: 10px;
            }
        `;
        document.head.appendChild(style);

        document.body.appendChild(alertDiv);

        // 自动移除
        if (duration > 0) {
            setTimeout(() => {
                if (alertDiv.parentNode) {
                    alertDiv.remove();
                }
            }, duration);
        }
    },

    // 显示加载状态
    showLoading: function(container = document.body, message = '加载中...') {
        const loadingDiv = document.createElement('div');
        loadingDiv.className = 'loading-overlay';
        loadingDiv.innerHTML = `
            <div class="loading">
                <div class="spinner"></div>
                <p>${message}</p>
            </div>
        `;
        loadingDiv.style.cssText = `
            position: absolute;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            background: rgba(255, 255, 255, 0.8);
            display: flex;
            align-items: center;
            justify-content: center;
            z-index: 1000;
        `;

        if (container === document.body) {
            loadingDiv.style.position = 'fixed';
        } else {
            container.style.position = 'relative';
        }

        container.appendChild(loadingDiv);
        return loadingDiv;
    },

    // 隐藏加载状态
    hideLoading: function(container = document.body) {
        const loading = container.querySelector('.loading-overlay');
        if (loading) {
            loading.remove();
        }
    },

    // 确认对话框
    confirm: function(message, callback) {
        const modal = this.createModal('确认操作', `
            <p>${message}</p>
        `, [
            { text: '取消', class: 'btn-secondary', onclick: 'closeModal()' },
            { text: '确认', class: 'btn-danger', onclick: () => { callback(); this.closeModal(); } }
        ]);
        this.showModal(modal);
    },

    // 创建模态框
    createModal: function(title, content, buttons = []) {
        const modal = document.createElement('div');
        modal.className = 'modal';
        modal.id = 'commonModal';

        let buttonHTML = '';
        buttons.forEach(btn => {
            const onclick = typeof btn.onclick === 'function' ? `onclick="(${btn.onclick})()"` : `onclick="${btn.onclick}"`;
            buttonHTML += `<button type="button" class="btn ${btn.class}" ${onclick}>${btn.text}</button>`;
        });

        modal.innerHTML = `
            <div class="modal-dialog">
                <div class="modal-header">
                    <h5 class="modal-title">${title}</h5>
                    <button type="button" class="modal-close" onclick="Utils.closeModal()">&times;</button>
                </div>
                <div class="modal-body">
                    ${content}
                </div>
                <div class="modal-footer">
                    ${buttonHTML}
                </div>
            </div>
        `;

        return modal;
    },

    // 显示模态框
    showModal: function(modal) {
        document.body.appendChild(modal);
        modal.classList.add('show');

        // 点击背景关闭
        modal.addEventListener('click', (e) => {
            if (e.target === modal) {
                this.closeModal();
            }
        });
    },

    // 关闭模态框
    closeModal: function() {
        const modal = document.getElementById('commonModal');
        if (modal) {
            modal.remove();
        }
    },

    // 表单验证
    validateForm: function(form) {
        let isValid = true;
        const inputs = form.querySelectorAll('input[required], select[required], textarea[required]');

        inputs.forEach(input => {
            if (!input.value.trim()) {
                this.showFieldError(input, '此字段不能为空');
                isValid = false;
            } else {
                this.clearFieldError(input);
            }
        });

        return isValid;
    },

    // 显示字段错误
    showFieldError: function(field, message) {
        field.classList.add('is-invalid');

        let feedback = field.parentNode.querySelector('.invalid-feedback');
        if (!feedback) {
            feedback = document.createElement('div');
            feedback.className = 'invalid-feedback';
            field.parentNode.appendChild(feedback);
        }
        feedback.textContent = message;
    },

    // 清除字段错误
    clearFieldError: function(field) {
        field.classList.remove('is-invalid');
        const feedback = field.parentNode.querySelector('.invalid-feedback');
        if (feedback) {
            feedback.remove();
        }
    },

    // 格式化日期
    formatDate: function(date, format = 'YYYY-MM-DD HH:mm:ss') {
        if (!date) return '';

        const d = new Date(date);
        const year = d.getFullYear();
        const month = String(d.getMonth() + 1).padStart(2, '0');
        const day = String(d.getDate()).padStart(2, '0');
        const hours = String(d.getHours()).padStart(2, '0');
        const minutes = String(d.getMinutes()).padStart(2, '0');
        const seconds = String(d.getSeconds()).padStart(2, '0');

        return format
            .replace('YYYY', year)
            .replace('MM', month)
            .replace('DD', day)
            .replace('HH', hours)
            .replace('mm', minutes)
            .replace('ss', seconds);
    },

    // 获取URL参数
    getUrlParam: function(name) {
        const urlParams = new URLSearchParams(window.location.search);
        return urlParams.get(name);
    },

    // 设置URL参数
    setUrlParam: function(name, value) {
        const url = new URL(window.location);
        url.searchParams.set(name, value);
        window.history.replaceState({}, '', url);
    },

    // 深拷贝对象
    deepClone: function(obj) {
        if (obj === null || typeof obj !== 'object') return obj;
        if (obj instanceof Date) return new Date(obj.getTime());
        if (obj instanceof Array) return obj.map(item => this.deepClone(item));
        if (typeof obj === 'object') {
            const clonedObj = {};
            for (const key in obj) {
                if (obj.hasOwnProperty(key)) {
                    clonedObj[key] = this.deepClone(obj[key]);
                }
            }
            return clonedObj;
        }
    },

    // 防抖函数
    debounce: function(func, wait) {
        let timeout;
        return function executedFunction(...args) {
            const later = () => {
                clearTimeout(timeout);
                func(...args);
            };
            clearTimeout(timeout);
            timeout = setTimeout(later, wait);
        };
    },

    // 节流函数
    throttle: function(func, limit) {
        let inThrottle;
        return function() {
            const args = arguments;
            const context = this;
            if (!inThrottle) {
                func.apply(context, args);
                inThrottle = true;
                setTimeout(() => inThrottle = false, limit);
            }
        };
    }
};

// 表格工具函数
const TableUtils = {
    // 创建表格
    createTable: function(data, columns, container) {
        const table = document.createElement('table');
        table.className = 'table table-striped table-hover';

        // 创建表头
        const thead = document.createElement('thead');
        const headerRow = document.createElement('tr');
        columns.forEach(col => {
            const th = document.createElement('th');
            th.textContent = col.title;
            if (col.width) th.style.width = col.width;
            headerRow.appendChild(th);
        });
        thead.appendChild(headerRow);
        table.appendChild(thead);

        // 创建表体
        const tbody = document.createElement('tbody');
        data.forEach(row => {
            const tr = document.createElement('tr');
            columns.forEach(col => {
                const td = document.createElement('td');
                if (col.render) {
                    td.innerHTML = col.render(row[col.field], row);
                } else {
                    td.textContent = row[col.field] || '';
                }
                tr.appendChild(td);
            });
            tbody.appendChild(tr);
        });
        table.appendChild(tbody);

        // 清空容器并添加表格
        container.innerHTML = '';
        container.appendChild(table);
    },

    // 创建分页
    createPagination: function(total, pageSize, currentPage, container, callback) {
        const totalPages = Math.ceil(total / pageSize);
        if (totalPages <= 1) {
            container.innerHTML = '';
            return;
        }

        let paginationHTML = '<div class="pagination">';

        // 上一页
        if (currentPage > 1) {
            paginationHTML += `<div class="page-item"><a class="page-link" href="#" data-page="${currentPage - 1}">上一页</a></div>`;
        } else {
            paginationHTML += `<div class="page-item disabled"><span class="page-link">上一页</span></div>`;
        }

        // 页码
        let startPage = Math.max(1, currentPage - 2);
        let endPage = Math.min(totalPages, startPage + 4);

        if (endPage - startPage < 4) {
            startPage = Math.max(1, endPage - 4);
        }

        if (startPage > 1) {
            paginationHTML += `<div class="page-item"><a class="page-link" href="#" data-page="1">1</a></div>`;
            if (startPage > 2) {
                paginationHTML += `<div class="page-item disabled"><span class="page-link">...</span></div>`;
            }
        }

        for (let i = startPage; i <= endPage; i++) {
            if (i === currentPage) {
                paginationHTML += `<div class="page-item active"><span class="page-link">${i}</span></div>`;
            } else {
                paginationHTML += `<div class="page-item"><a class="page-link" href="#" data-page="${i}">${i}</a></div>`;
            }
        }

        if (endPage < totalPages) {
            if (endPage < totalPages - 1) {
                paginationHTML += `<div class="page-item disabled"><span class="page-link">...</span></div>`;
            }
            paginationHTML += `<div class="page-item"><a class="page-link" href="#" data-page="${totalPages}">${totalPages}</a></div>`;
        }

        // 下一页
        if (currentPage < totalPages) {
            paginationHTML += `<div class="page-item"><a class="page-link" href="#" data-page="${currentPage + 1}">下一页</a></div>`;
        } else {
            paginationHTML += `<div class="page-item disabled"><span class="page-link">下一页</span></div>`;
        }

        paginationHTML += '</div>';
        container.innerHTML = paginationHTML;

        // 绑定点击事件
        container.addEventListener('click', (e) => {
            e.preventDefault();
            if (e.target.tagName === 'A' && e.target.dataset.page) {
                callback(parseInt(e.target.dataset.page));
            }
        });
    }
};

// 表单工具函数
const FormUtils = {
    // 序列化表单数据
    serialize: function(form) {
        const formData = new FormData(form);
        const data = {};
        for (let [key, value] of formData.entries()) {
            if (data[key]) {
                if (Array.isArray(data[key])) {
                    data[key].push(value);
                } else {
                    data[key] = [data[key], value];
                }
            } else {
                data[key] = value;
            }
        }
        return data;
    },

    // 填充表单数据
    populate: function(form, data) {
        Object.keys(data).forEach(key => {
            const field = form.querySelector(`[name="${key}"]`);
            if (field) {
                if (field.type === 'checkbox' || field.type === 'radio') {
                    field.checked = field.value === String(data[key]);
                } else {
                    field.value = data[key] || '';
                }
            }
        });
    },

    // 重置表单
    reset: function(form) {
        form.reset();
        // 清除验证状态
        const invalidFields = form.querySelectorAll('.is-invalid');
        invalidFields.forEach(field => {
            Utils.clearFieldError(field);
        });
    }
};

// 存储工具函数
const StorageUtils = {
    // 设置本地存储
    setLocal: function(key, value) {
        try {
            localStorage.setItem(key, JSON.stringify(value));
        } catch (e) {
            console.warn('Local storage is not available:', e);
        }
    },

    // 获取本地存储
    getLocal: function(key, defaultValue = null) {
        try {
            const item = localStorage.getItem(key);
            return item ? JSON.parse(item) : defaultValue;
        } catch (e) {
            console.warn('Error reading from local storage:', e);
            return defaultValue;
        }
    },

    // 删除本地存储
    removeLocal: function(key) {
        try {
            localStorage.removeItem(key);
        } catch (e) {
            console.warn('Error removing from local storage:', e);
        }
    },

    // 设置会话存储
    setSession: function(key, value) {
        try {
            sessionStorage.setItem(key, JSON.stringify(value));
        } catch (e) {
            console.warn('Session storage is not available:', e);
        }
    },

    // 获取会话存储
    getSession: function(key, defaultValue = null) {
        try {
            const item = sessionStorage.getItem(key);
            return item ? JSON.parse(item) : defaultValue;
        } catch (e) {
            console.warn('Error reading from session storage:', e);
            return defaultValue;
        }
    },

    // 删除会话存储
    removeSession: function(key) {
        try {
            sessionStorage.removeItem(key);
        } catch (e) {
            console.warn('Error removing from session storage:', e);
        }
    }
};

// 初始化公共功能
document.addEventListener('DOMContentLoaded', function() {
    // 全局错误处理
    window.addEventListener('error', function(e) {
        console.error('Global error:', e.error);
        Utils.showMessage('系统出现错误，请刷新页面重试', 'danger');
    });

    // 全局未捕获的Promise错误
    window.addEventListener('unhandledrejection', function(e) {
        console.error('Unhandled promise rejection:', e.reason);
        Utils.showMessage('请求失败，请检查网络连接', 'danger');
        e.preventDefault();
    });
});

import axios from 'axios'
import router from "../router";

const request = axios.create({
    baseURL: '/api',
    timeout: 5000
})
window.onerror = function(message, url, lineNumber) {
    // 在这里处理错误
    console.error("发生未捕获的错误:", message);
    // 可以发送错误信息到服务器进行记录
    // 或者显示一个错误提示给用户
  };
  
request.interceptors.request.use(
    config => {
        // config.headers['Content-Type']='application/json; charset=utf-8; ';
        // 管理端用 backUser，前端业主用 currentUser，都带 token
        const backUser = sessionStorage.getItem('backUser');
        const currentUser = sessionStorage.getItem('currentUser');
        const user = backUser ? JSON.parse(backUser) : (currentUser ? JSON.parse(currentUser) : null);
        if (user && user.token) {
            config.headers['token'] = user.token;
        }
        return config;
    },
    error => {
        // 请求错误时做些什么
        console.error('request error:', error);
        return Promise.reject(error);
    }
);
request.interceptors.response.use(
    response => {
        // 处理文件下载等特殊响应类型
        if (response.config.responseType === 'blob') {
            return response;
        }

        // 确保返回的数据是JSON格式
        let res = response.data;
        if (typeof res === 'string') {
            res = JSON.parse(res);
        }

        // 根据响应状态码处理不同的逻辑
        switch (response.status) {
            case 200:
                // 正常逻辑处理，明确返回res
                return res;
            case 500:
            case 403:
            case 408:
            case 401:
                // 使用统一的错误处理函数处理这些状态码
                handleErrorResponse(response.status);
                // 对于这些错误状态码，可以选择不返回res，或者根据实际情况返回一个特定的值或错误对象
                break;
            // 可以继续添加其他状态码的处理
            default:
                // 其他状态码的处理
                break;
        }
    },
    error => {
        // 错误处理
        let status, data;
        if (error.response) {
            ({ status, data } = error.response);
        } else {
            status = null;
            data = error.message || '请求发生错误，但未收到响应';
        }
        console.error("请求发生错误", status, data);
        const skipGlobalError = error.config && error.config.skipGlobalError === true;
        if (!skipGlobalError) {
            handleErrorResponse(status, data);
        }
        return Promise.reject(error);
    }
);

// 统一的错误处理函数
function handleErrorResponse(status, data) {
    let message = '';
    if (status === null) {
        message = data; // 使用传入的data作为错误信息
    } else {
        switch (status) {
            case 500:
                message = '服务器内部错误，请稍后再试！';
                break;
            case 403:
                message = '您没有权限访问该资源！';
                break;
            case 408:
                message = '请求超时，请检查您的网络连接！';
                break;
            case 401:
                message = '登录失效，请重新登录！';
                break;
            // 可以继续补充其他状态码的错误信息
            default:
                message = '请求发生错误，请稍后再试！';
                break;
        }
    }
    // 使用模态框组件或其他更友好的方式展示错误信息
    alert(message); // 这里仅为示例，实际项目中建议使用更友好的提示方式
}

export default request


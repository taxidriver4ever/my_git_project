import { contextBridge, ipcRenderer } from 'electron'
import { electronAPI } from '@electron-toolkit/preload'

// 自定义API
const api = {
  // 通过主进程发送axios请求
  axiosRequest: (config) => ipcRenderer.invoke('axios-request', config),
  
  // 其他自定义API...
}

// 使用contextBridge将API暴露给渲染进程
if (process.contextIsolated) {
  try {
    contextBridge.exposeInMainWorld('electron', electronAPI)
    contextBridge.exposeInMainWorld('api', api)
  } catch (error) {
    console.error(error)
  }
} else {
  window.Electron = electronAPI
  window.api = api
}

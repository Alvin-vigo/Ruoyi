import { MessageBox, Message, Notification } from 'element-ui'

/**
 * 全局弹窗 / 消息封装（若依标准）
 */
const Modal = {
  alert(message, title, options) {
    return MessageBox.alert(message, title, options)
  },
  confirm(message, title, options) {
    return MessageBox.confirm(message, title, options)
  },
  prompt(message, title, options) {
    return MessageBox.prompt(message, title, options)
  },
  msgSuccess(msg) {
    Message.success(msg)
  },
  msgError(msg) {
    Message.error(msg)
  },
  msgInfo(msg) {
    Message.info(msg)
  },
  msgWarning(msg) {
    Message.warning(msg)
  },
  msgBox(options) {
    return MessageBox(options)
  },
  notifySuccess(msg) {
    Notification.success(msg)
  },
  notifyError(msg) {
    Notification.error(msg)
  },
  notifyInfo(msg) {
    Notification.info(msg)
  },
  notifyWarning(msg) {
    Notification.warning(msg)
  }
}
export default Modal

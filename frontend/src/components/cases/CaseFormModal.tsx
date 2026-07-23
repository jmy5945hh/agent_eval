import { Form, Input, InputNumber, Modal, Select, message } from 'antd';
import { CATEGORIES } from '../../mock/data';

interface CaseFormModalProps {
  open: boolean;
  onCancel: () => void;
}

export function CaseFormModal({ open, onCancel }: CaseFormModalProps) {
  const [form] = Form.useForm();

  return (
    <Modal
      open={open}
      title="新增测评案例"
      width={720}
      onCancel={onCancel}
      okText="保存案例"
      cancelText="取消"
      onOk={() => {
        form.validateFields().then(() => {
          message.success('案例已保存（Mock）');
          form.resetFields();
          onCancel();
        });
      }}
    >
      <Form form={form} layout="vertical" className="modal-form" initialValues={{ difficulty: '中', category: '前端', version: 1 }}>
        <div className="form-grid">
          <Form.Item name="name" label="案例名称" rules={[{ required: true, message: '请输入案例名称' }]}>
            <Input maxLength={20} placeholder="例如：实现响应式商品卡片" />
          </Form.Item>
          <Form.Item name="category" label="案例分类" rules={[{ required: true }]}>
            <Select options={CATEGORIES.map((value) => ({ value, label: value }))} />
          </Form.Item>
        </div>
        <Form.Item name="prompt" label="Prompt 描述" rules={[{ required: true, message: '请输入完整 Prompt' }]}>
          <Input.TextArea rows={5} placeholder="输入将提供给 Agent 的完整任务描述与验收要求" />
        </Form.Item>
        <div className="form-grid">
          <Form.Item name="repo" label="目标仓库" rules={[{ required: true }]}>
            <Input placeholder="git.example.com/team/project" />
          </Form.Item>
          <Form.Item name="branch" label="目标分支" rules={[{ required: true }]}>
            <Input placeholder="main" />
          </Form.Item>
          <Form.Item name="difficulty" label="难度">
            <Select options={['高', '中', '低'].map((value) => ({ value, label: value }))} />
          </Form.Item>
          <Form.Item name="version" label="版本">
            <InputNumber min={1} style={{ width: '100%' }} />
          </Form.Item>
        </div>
        <Form.Item name="answer" label="标准答案" rules={[{ required: true, message: '请填写标准答案或外联地址' }]}>
          <Input.TextArea rows={3} placeholder="代码内容、相对路径或标准答案外联地址" />
        </Form.Item>
      </Form>
    </Modal>
  );
}

import { Button, Form, Input, InputNumber, Modal, Select, Upload, message } from 'antd';
import { DeleteOutlined, PlusOutlined, UploadOutlined } from '@ant-design/icons';
import { CATEGORIES } from '../../mock/data';
import type { EvaluationCase } from '../../types';

interface CaseFormModalProps {
  open: boolean;
  item?: EvaluationCase | null;
  onCancel: () => void;
}

export function CaseFormModal({ open, item, onCancel }: CaseFormModalProps) {
  const [form] = Form.useForm();

  return (
    <Modal
      open={open}
      title={item ? '编辑测评案例' : '新增测评案例'}
      width={720}
      onCancel={onCancel}
      okText="保存案例"
      cancelText="取消"
      destroyOnHidden
      afterOpenChange={(nextOpen) => {
        if (!nextOpen) return;
        form.setFieldsValue(item ? {
          ...item,
          standardAnswers: item.standardAnswer.map((answer, index) => ({
            path: answer.path,
            file: [{
              uid: `existing-${index}`,
              name: answer.path.split('/').pop() || answer.path,
              status: 'done',
            }],
          })),
        } : {
          difficulty: '中',
          category: '前端',
          version: 1,
          standardAnswers: [{}],
        });
      }}
      onOk={() => {
        form.validateFields().then(() => {
          message.success('案例已保存（Mock）');
          form.resetFields();
          onCancel();
        });
      }}
    >
      <Form form={form} layout="vertical" className="modal-form">
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
        <div className="answer-list-head">
          <div><b>标准答案</b><small>每条答案需指定文件路径，并上传对应的本地代码文件</small></div>
        </div>
        <Form.List name="standardAnswers">
          {(fields, { add, remove }) => (
            <div className="answer-file-list">
              {fields.map((field, index) => (
                <div className="answer-file-row" key={field.key}>
                  <span className="answer-file-index">{String(index + 1).padStart(2, '0')}</span>
                  <Form.Item
                    {...field}
                    name={[field.name, 'path']}
                    label="文件路径参数"
                    rules={[{ required: true, message: '请输入文件路径' }]}
                  >
                    <Input placeholder="例如：src/components/ProductCard.tsx" />
                  </Form.Item>
                  <Form.Item
                    {...field}
                    name={[field.name, 'file']}
                    label="本地代码文件"
                    valuePropName="fileList"
                    getValueFromEvent={(event) => event?.fileList}
                    rules={[{ required: true, message: '请上传代码文件' }]}
                  >
                    <Upload beforeUpload={() => false} maxCount={1}>
                      <Button icon={<UploadOutlined />}>选择文件</Button>
                    </Upload>
                  </Form.Item>
                  <Button
                    type="text"
                    danger
                    aria-label="删除标准答案"
                    icon={<DeleteOutlined />}
                    disabled={fields.length === 1}
                    onClick={() => remove(field.name)}
                  />
                </div>
              ))}
              <Button type="dashed" block icon={<PlusOutlined />} onClick={() => add({})}>
                添加一条标准答案
              </Button>
            </div>
          )}
        </Form.List>
      </Form>
    </Modal>
  );
}

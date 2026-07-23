import { useState } from 'react';
import {
  Badge,
  Button,
  Form,
  Input,
  InputNumber,
  Modal,
  Space,
  Table,
  Tabs,
  Tag,
  message,
} from 'antd';
import {
  DatabaseOutlined,
  DeleteOutlined,
  EditOutlined,
  FileSearchOutlined,
  PlusOutlined,
  RobotOutlined,
} from '@ant-design/icons';
import { AGENTS, MODELS, SCORING_STANDARDS } from '../mock/data';
import type { AgentProduct, ModelConfig, ScoringStandard } from '../types';

function StandardEditor({
  standard,
  onClose,
  onSave,
}: {
  standard: ScoringStandard | null;
  onClose: () => void;
  onSave: (standard: ScoringStandard) => void;
}) {
  const [form] = Form.useForm();

  return (
    <Modal
      open={Boolean(standard)}
      title={`编辑评分标准 · ${standard?.version || ''}`}
      width={760}
      onCancel={onClose}
      okText="保存修改"
      destroyOnHidden
      afterOpenChange={(open) => {
        if (open && standard) {
          form.setFieldsValue({
            version: standard.version,
            note: standard.note,
            dimensions: standard.dimensions,
          });
        }
      }}
      onOk={() => {
        form.validateFields().then((values) => {
          const total = values.dimensions.reduce((sum: number, item: { weight: number }) => sum + Number(item.weight), 0);
          if (total !== 100) {
            message.error(`当前维度权重合计为 ${total}%，请调整为 100%`);
            return;
          }
          if (standard) {
            onSave({ ...standard, ...values, updatedAt: '2026-07-23 16:20' });
          }
        });
      }}
    >
      <Form form={form} layout="vertical" className="standard-editor-form">
        <div className="form-grid">
          <Form.Item name="version" label="版本号" rules={[{ required: true }]}>
            <Input disabled />
          </Form.Item>
          <Form.Item name="note" label="版本说明" rules={[{ required: true, message: '请输入版本说明' }]}>
            <Input placeholder="说明本次评分规则的适用范围" />
          </Form.Item>
        </div>
        <div className="dimension-editor-head">
          <div><b>评分维度</b><small>所有维度权重之和必须为 100%</small></div>
        </div>
        <Form.List name="dimensions">
          {(fields, { add, remove }) => (
            <div className="dimension-editor-list">
              {fields.map((field) => (
                <div className="dimension-editor-row" key={field.key}>
                  <Form.Item {...field} name={[field.name, 'label']} rules={[{ required: true, message: '请输入维度名称' }]}>
                    <Input placeholder="维度名称" />
                  </Form.Item>
                  <Form.Item {...field} name={[field.name, 'key']} hidden>
                    <Input />
                  </Form.Item>
                  <Form.Item {...field} name={[field.name, 'weight']} rules={[{ required: true, message: '请输入权重' }]}>
                    <InputNumber min={1} max={100} addonAfter="%" />
                  </Form.Item>
                  <Form.Item {...field} name={[field.name, 'desc']} rules={[{ required: true, message: '请输入维度说明' }]}>
                    <Input placeholder="描述评分关注点" />
                  </Form.Item>
                  <Button
                    danger
                    type="text"
                    icon={<DeleteOutlined />}
                    aria-label="删除评分维度"
                    disabled={fields.length <= 1}
                    onClick={() => remove(field.name)}
                  />
                </div>
              ))}
              <Button
                block
                type="dashed"
                icon={<PlusOutlined />}
                onClick={() => add({ key: `custom-${Date.now()}`, label: '', weight: 10, desc: '' })}
              >
                添加评分维度
              </Button>
            </div>
          )}
        </Form.List>
      </Form>
    </Modal>
  );
}

export function SettingsPage() {
  const [standards, setStandards] = useState<ScoringStandard[]>(SCORING_STANDARDS);
  const [editingStandard, setEditingStandard] = useState<ScoringStandard | null>(null);

  const agentColumns = [
    { title: 'Agent', dataIndex: 'name', render: (_: string, agent: AgentProduct) => <div className="agent-cell"><span><RobotOutlined /></span><div><b>{agent.name}</b><small>{agent.vendor}</small></div></div> },
    { title: '默认版本', dataIndex: 'version', width: 130 },
    { title: '状态', dataIndex: 'status', width: 120, render: () => <Badge status="success" text="已启用" /> },
    { title: '说明', dataIndex: 'desc', ellipsis: true },
    { title: '', width: 100, render: () => <Button type="text" icon={<EditOutlined />}>编辑</Button> },
  ];
  const modelColumns = [
    { title: '模型', dataIndex: 'name', render: (_: string, model: ModelConfig) => <div className="agent-cell"><span><DatabaseOutlined /></span><div><b>{model.name}</b><small>{model.provider}</small></div></div> },
    { title: '类型', dataIndex: 'tier', width: 100, render: (value: string) => <Tag className="soft-tag">{value}</Tag> },
    { title: '版本', dataIndex: 'version', width: 120 },
    { title: '用途', dataIndex: 'scoring', width: 120, render: (value: boolean) => value ? '评分模型' : '测评模型' },
    { title: '状态', dataIndex: 'enabled', width: 110, render: (value: boolean) => <Badge status={value ? 'success' : 'default'} text={value ? '已启用' : '未启用'} /> },
    { title: '', width: 100, render: () => <Button type="text" icon={<EditOutlined />}>编辑</Button> },
  ];

  const saveStandard = (updated: ScoringStandard) => {
    setStandards((current) => current.map((item) => item.id === updated.id ? updated : item));
    setEditingStandard(null);
    message.success('评分标准已更新（Mock）');
  };

  return (
    <div className="page-stack">
      <section className="surface-card settings-card">
        <Tabs
          size="large"
          items={[
            {
              key: 'agents',
              label: <span><RobotOutlined /> Agent 管理</span>,
              children: (
                <>
                  <div className="section-head"><div><h3>参测 Agent</h3><p>一期固定支持 Pi Agent、DevAgent CLI 与 OpenCode</p></div><Button icon={<PlusOutlined />}>接入 Agent</Button></div>
                  <Table dataSource={AGENTS} columns={agentColumns} rowKey="id" pagination={false} scroll={{ x: 760 }} />
                </>
              ),
            },
            {
              key: 'models',
              label: <span><DatabaseOutlined /> 模型配置</span>,
              children: (
                <>
                  <div className="section-head"><div><h3>模型列表</h3><p>配置测评模型与独立评分模型</p></div><Button type="primary" icon={<PlusOutlined />}>新增模型</Button></div>
                  <Table dataSource={MODELS} columns={modelColumns} rowKey="id" pagination={{ pageSize: 8, showSizeChanger: false }} scroll={{ x: 850 }} />
                </>
              ),
            },
            {
              key: 'standards',
              label: <span><FileSearchOutlined /> 评分标准</span>,
              children: (
                <>
                  <div className="section-head">
                    <div><h3>评分标准版本</h3><p>维护评分维度、权重与版本说明</p></div>
                    <Button icon={<PlusOutlined />}>创建新版本</Button>
                  </div>
                  <div className="standards-grid">
                    {standards.map((standard) => (
                      <div className={`standard-card ${standard.current ? 'current' : ''}`} key={standard.id}>
                        <div><span>{standard.version}</span><Space>{standard.current && <Tag color="success">当前版本</Tag>}<Button type="text" size="small" icon={<EditOutlined />} onClick={() => setEditingStandard(standard)}>编辑</Button></Space></div>
                        <p>{standard.note}</p>
                        {standard.dimensions.map((dimension) => <div className="standard-dim" key={dimension.key}><span>{dimension.label}</span><b>{dimension.weight}%</b></div>)}
                        <small>更新于 {standard.updatedAt}</small>
                      </div>
                    ))}
                  </div>
                </>
              ),
            },
          ]}
        />
      </section>
      <StandardEditor standard={editingStandard} onClose={() => setEditingStandard(null)} onSave={saveStandard} />
    </div>
  );
}

import { Button, Drawer, Modal, Tag } from 'antd';
import { BranchesOutlined, EditOutlined } from '@ant-design/icons';
import type { EvaluationCase } from '../../types';
import { categoryIcons, difficultyClass } from './caseVisuals';

function CaseDetailContent({ item }: { item: EvaluationCase }) {
  return (
    <div className="drawer-stack">
      <div className="case-detail-hero">
        <div className="case-type-icon large">{categoryIcons[item.category]}</div>
        <div>
          <Tag className="soft-tag">{item.category}</Tag>
          <span className={`level-pill ${difficultyClass[item.difficulty]}`}><i />{item.difficulty}难度</span>
        </div>
        <p>{item.code} · 版本 v{item.version} · 创建于 {item.createdAt}</p>
      </div>
      <section>
        <h4>Prompt 描述</h4>
        <div className="prompt-box">{item.prompt}</div>
      </section>
      <section>
        <h4>执行目标</h4>
        <div className="detail-list">
          <div><span>仓库</span><b>{item.repo}</b></div>
          <div><span>分支</span><b><BranchesOutlined /> {item.branch}</b></div>
          <div><span>重要性</span><b>{item.importance}</b></div>
        </div>
      </section>
      <section>
        <h4>标准答案 <Tag>{item.standardAnswer.length} 个文件</Tag></h4>
        {item.standardAnswer.map((file) => (
          <div className="code-file" key={file.path}>
            <div className="code-file-head"><span>{file.path}</span><span>标准实现</span></div>
            <pre className="code-block">{file.content}</pre>
          </div>
        ))}
      </section>
    </div>
  );
}

export function CaseDrawer({ item, onClose }: { item: EvaluationCase | null; onClose: () => void }) {
  return (
    <Drawer
      open={Boolean(item)}
      onClose={onClose}
      width={640}
      title={item?.name}
      extra={<Button icon={<EditOutlined />}>编辑</Button>}
    >
      {item && <CaseDetailContent item={item} />}
    </Drawer>
  );
}

export function CaseDetailModal({ item, onClose }: { item: EvaluationCase | null; onClose: () => void }) {
  return (
    <Modal
      open={Boolean(item)}
      title={item?.name}
      width={760}
      onCancel={onClose}
      footer={<Button type="primary" onClick={onClose}>关闭</Button>}
      styles={{ body: { maxHeight: '68vh', overflowY: 'auto', paddingTop: 12 } }}
    >
      {item && <CaseDetailContent item={item} />}
    </Modal>
  );
}

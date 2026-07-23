import { BarChartOutlined } from '@ant-design/icons';
import { Modal, Radio, Select, message } from 'antd';
import { MODELS, SCORING_STANDARDS } from '../../mock/data';

export function ScoringModal({ open, onClose }: { open: boolean; onClose: () => void }) {
  return (
    <Modal
      open={open}
      onCancel={onClose}
      title="发起自动评分"
      okText="开始评分"
      onOk={() => {
        message.success('评分任务已进入队列（Mock）');
        onClose();
      }}
    >
      <div className="scoring-form">
        <label>评分模型</label>
        <Radio.Group defaultValue="judge-pro">
          {MODELS.filter((model) => model.scoring).map((model) => (
            <Radio.Button value={model.id} key={model.id}>{model.name}</Radio.Button>
          ))}
        </Radio.Group>
        <label>评分标准</label>
        <Select
          defaultValue="v2.0"
          options={SCORING_STANDARDS.map((standard) => ({
            value: standard.version,
            label: `${standard.version} · ${standard.note}`,
          }))}
        />
        <div className="queue-tip">
          <BarChartOutlined />
          <p><b>按案例进入评分队列</b><br />评分模型会基于标准答案逐条输出维度得分、评语和建议。</p>
        </div>
      </div>
    </Modal>
  );
}

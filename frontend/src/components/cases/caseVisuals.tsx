import {
  CloudServerOutlined,
  CodeOutlined,
  DatabaseOutlined,
  RobotOutlined,
  SafetyCertificateOutlined,
} from '@ant-design/icons';
import type { ReactNode } from 'react';

export const categoryIcons: Record<string, ReactNode> = {
  前端: <CodeOutlined />,
  Java后端: <CloudServerOutlined />,
  Python后端: <DatabaseOutlined />,
  AI智能体: <RobotOutlined />,
  安全测试: <SafetyCertificateOutlined />,
};

export const difficultyClass: Record<string, string> = {
  高: 'danger',
  中: 'warning',
  低: 'success',
};

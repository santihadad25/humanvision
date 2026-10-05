import { AlertIcon, InfoIcon } from './icons.jsx'

const TONES = {
  error: { Icon: AlertIcon, role: 'alert' },
  info: { Icon: InfoIcon, role: 'status' },
}

export default function Notice({ tone, children }) {
  const { Icon, role } = TONES[tone]
  return (
    <div className={`notice notice-${tone}`} role={role}>
      <Icon />
      <div>{children}</div>
    </div>
  )
}

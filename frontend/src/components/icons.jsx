const baseProps = {
  width: 20,
  height: 20,
  viewBox: '0 0 24 24',
  fill: 'none',
  stroke: 'currentColor',
  strokeWidth: 2,
  strokeLinecap: 'round',
  strokeLinejoin: 'round',
  'aria-hidden': true,
}

export function UploadIcon(props) {
  return (
    <svg {...baseProps} {...props}>
      <path d="M12 16V4m0 0L7 9m5-5 5 5" />
      <path d="M4 16v3a1 1 0 0 0 1 1h14a1 1 0 0 0 1-1v-3" />
    </svg>
  )
}

export function FileIcon(props) {
  return (
    <svg {...baseProps} {...props}>
      <path d="M14 3H7a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h10a2 2 0 0 0 2-2V8z" />
      <path d="M14 3v5h5" />
    </svg>
  )
}

export function CheckSquareIcon(props) {
  return (
    <svg {...baseProps} {...props}>
      <rect x="4" y="4" width="16" height="16" rx="3" />
      <path d="m8.5 12.5 2.5 2.5 4.5-5" />
    </svg>
  )
}

export function SquareIcon(props) {
  return (
    <svg {...baseProps} {...props}>
      <rect x="4" y="4" width="16" height="16" rx="3" />
    </svg>
  )
}

export function LayersIcon(props) {
  return (
    <svg {...baseProps} {...props}>
      <path d="m12 3 9 5-9 5-9-5z" />
      <path d="m3 13 9 5 9-5" />
    </svg>
  )
}

export function AlertIcon(props) {
  return (
    <svg {...baseProps} {...props}>
      <path d="M12 3 2 20h20z" />
      <path d="M12 10v4m0 3h.01" />
    </svg>
  )
}

export function InfoIcon(props) {
  return (
    <svg {...baseProps} {...props}>
      <circle cx="12" cy="12" r="9" />
      <path d="M12 11v5m0-8h.01" />
    </svg>
  )
}

export function CloseIcon(props) {
  return (
    <svg {...baseProps} {...props}>
      <path d="m6 6 12 12M18 6 6 18" />
    </svg>
  )
}

import { formatFileSize } from '../../lib/formatFileSize.js'
import { CloseIcon, FileIcon } from '../icons.jsx'

export default function SelectedFile({ file, disabled, onRemove }) {
  return (
    <div className="file-chip">
      <span className="file-chip-icon">
        <FileIcon />
      </span>
      <span className="file-chip-text">
        <strong title={file.name}>{file.name}</strong>
        <small>{formatFileSize(file.size)}</small>
      </span>
      <button type="button" className="icon-button" onClick={onRemove} disabled={disabled} aria-label="Remove file">
        <CloseIcon width={18} height={18} />
      </button>
    </div>
  )
}

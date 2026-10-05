import { useState } from 'react'
import { ACCEPTED_MIME_TYPES, MAX_UPLOAD_BYTES } from '../../config.js'
import { formatFileSize } from '../../lib/formatFileSize.js'
import { UploadIcon } from '../icons.jsx'
import SelectedFile from './SelectedFile.jsx'

export default function FileDropzone({ file, disabled, onFileChosen, onFileCleared }) {
  const [isDraggingOver, setIsDraggingOver] = useState(false)

  function handleDragOver(event) {
    event.preventDefault()
    setIsDraggingOver(true)
  }

  function handleDrop(event) {
    event.preventDefault()
    setIsDraggingOver(false)
    const dropped = event.dataTransfer.files[0]
    if (dropped && !disabled) onFileChosen(dropped)
  }

  function handleInputChange(event) {
    const chosen = event.target.files[0]
    event.target.value = ''
    if (chosen) onFileChosen(chosen)
  }

  if (file) return <SelectedFile file={file} disabled={disabled} onRemove={onFileCleared} />

  return (
    <label
      className={`dropzone${isDraggingOver ? ' is-dragging' : ''}`}
      onDragOver={handleDragOver}
      onDragLeave={() => setIsDraggingOver(false)}
      onDrop={handleDrop}
    >
      <input
        className="visually-hidden"
        type="file"
        accept={ACCEPTED_MIME_TYPES.join(',')}
        onChange={handleInputChange}
        disabled={disabled}
      />
      <span className="dropzone-icon">
        <UploadIcon width={28} height={28} />
      </span>
      <span className="dropzone-title">
        <strong>Choose a file</strong> or drag it here
      </span>
      <span className="dropzone-hint">PNG, JPEG or PDF, up to {formatFileSize(MAX_UPLOAD_BYTES)}</span>
    </label>
  )
}

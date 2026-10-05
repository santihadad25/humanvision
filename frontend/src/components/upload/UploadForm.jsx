import FileDropzone from './FileDropzone.jsx'

export default function UploadForm({
  file,
  canSubmit,
  isDetecting,
  isPreparingPreview,
  onFileChosen,
  onFileCleared,
  onSubmit,
}) {
  function handleSubmit(event) {
    event.preventDefault()
    if (canSubmit) onSubmit()
  }

  return (
    <form className="card upload-card" onSubmit={handleSubmit}>
      <FileDropzone file={file} disabled={isDetecting} onFileChosen={onFileChosen} onFileCleared={onFileCleared} />
      <div className="upload-actions">
        <button type="submit" className="primary-button" disabled={!canSubmit}>
          {isDetecting && <span className="spinner" aria-hidden="true" />}
          {isDetecting ? 'Analysing…' : 'Detect checkboxes'}
        </button>
        <span className="status" role="status" aria-live="polite">
          {isPreparingPreview && 'Preparing the preview…'}
          {isDetecting && 'Looking for checkboxes. Large pages can take a few seconds.'}
        </span>
      </div>
    </form>
  )
}

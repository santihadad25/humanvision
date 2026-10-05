import { useState } from 'react'
import Header from './components/Header.jsx'
import Notice from './components/Notice.jsx'
import Results from './components/results/Results.jsx'
import UploadForm from './components/upload/UploadForm.jsx'
import { useDetection } from './hooks/useDetection.js'
import { useDocumentPreview } from './hooks/useDocumentPreview.js'
import { validateFile } from './lib/validateFile.js'

export default function App() {
  const [selectedFile, setSelectedFile] = useState(null)
  const [validationError, setValidationError] = useState(null)
  const preview = useDocumentPreview(selectedFile)
  const detector = useDetection()

  function chooseFile(file) {
    detector.reset()
    const error = validateFile(file)
    setValidationError(error)
    setSelectedFile(error ? null : file)
  }

  function clearFile() {
    detector.reset()
    setValidationError(null)
    setSelectedFile(null)
  }

  const errorMessage = validationError ?? preview.error ?? detector.error
  const canDetect = Boolean(selectedFile) && !preview.error && !preview.isPreparing && !detector.isDetecting

  return (
    <div className="app">
      <Header />
      <main>
        <UploadForm
          file={selectedFile}
          canSubmit={canDetect}
          isDetecting={detector.isDetecting}
          isPreparingPreview={preview.isPreparing}
          onFileChosen={chooseFile}
          onFileCleared={clearFile}
          onSubmit={() => detector.detect(selectedFile)}
        />
        {errorMessage && <Notice tone="error">{errorMessage}</Notice>}
        <Results pagePreviewUrls={preview.pageUrls} detection={detector.detection} />
      </main>
      <footer className="app-footer">PNG, JPEG and PDF are processed on the server and are not stored.</footer>
    </div>
  )
}

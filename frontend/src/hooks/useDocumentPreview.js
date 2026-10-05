import { useEffect, useState } from 'react'
import { PDF_MIME_TYPE } from '../config.js'
import { renderPdfPages } from '../lib/renderPdfPages.js'

export function useDocumentPreview(file) {
  const [pageUrls, setPageUrls] = useState([])
  const [isPreparing, setIsPreparing] = useState(false)
  const [error, setError] = useState(null)

  useEffect(() => {
    setPageUrls([])
    setError(null)
    if (!file) return
    if (file.type !== PDF_MIME_TYPE) {
      const objectUrl = URL.createObjectURL(file)
      setPageUrls([objectUrl])
      return () => URL.revokeObjectURL(objectUrl)
    }
    let isCancelled = false
    setIsPreparing(true)
    renderPdfPages(file)
      .then((renderedUrls) => !isCancelled && setPageUrls(renderedUrls))
      .catch((renderError) => !isCancelled && setError(renderError.message || 'Could not preview the PDF.'))
      .finally(() => !isCancelled && setIsPreparing(false))
    return () => {
      isCancelled = true
    }
  }, [file])

  return { pageUrls, isPreparing, error }
}

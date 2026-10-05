import * as pdfjs from 'pdfjs-dist'
import workerUrl from 'pdfjs-dist/build/pdf.worker.min.mjs?url'
import { MAX_PDF_PAGES, PDF_PREVIEW_WIDTH_PX } from '../config.js'

pdfjs.GlobalWorkerOptions.workerSrc = workerUrl

export async function renderPdfPages(file) {
  const pdf = await openPdf(file)
  try {
    if (pdf.numPages === 0) {
      throw new Error('PDF has no pages.')
    }
    if (pdf.numPages > MAX_PDF_PAGES) {
      throw new Error(`PDF has ${pdf.numPages} pages; the maximum is ${MAX_PDF_PAGES}.`)
    }
    const pageUrls = []
    for (let pageNumber = 1; pageNumber <= pdf.numPages; pageNumber++) {
      pageUrls.push(await renderPage(await pdf.getPage(pageNumber)))
    }
    return pageUrls
  } finally {
    pdf.destroy()
  }
}

async function openPdf(file) {
  try {
    return await pdfjs.getDocument({ data: await file.arrayBuffer(), isEvalSupported: false }).promise
  } catch (error) {
    if (error.name === 'PasswordException') {
      throw new Error('Password-protected PDFs are not supported.')
    }
    throw error
  }
}

async function renderPage(page) {
  const naturalViewport = page.getViewport({ scale: 1 })
  const viewport = page.getViewport({ scale: PDF_PREVIEW_WIDTH_PX / naturalViewport.width })
  const canvas = document.createElement('canvas')
  canvas.width = Math.floor(viewport.width)
  canvas.height = Math.floor(viewport.height)
  await page.render({ canvasContext: canvas.getContext('2d'), viewport }).promise
  return canvas.toDataURL('image/png')
}

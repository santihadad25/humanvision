// Vercel Functions reject request bodies above 4.5 MB, so the Vercel build sets VITE_MAX_UPLOAD_MB=4.
// Docker Compose leaves it unset and keeps the backend's 10 MB.
export const MAX_UPLOAD_BYTES = (Number(import.meta.env.VITE_MAX_UPLOAD_MB) || 10) * 1024 * 1024
export const MAX_PDF_PAGES = 20

export const PDF_MIME_TYPE = 'application/pdf'
export const ACCEPTED_MIME_TYPES = ['image/png', 'image/jpeg', PDF_MIME_TYPE]

export const PDF_PREVIEW_WIDTH_PX = 900

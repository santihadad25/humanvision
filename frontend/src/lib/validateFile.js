import { ACCEPTED_MIME_TYPES, MAX_UPLOAD_BYTES } from '../config.js'
import { formatFileSize } from './formatFileSize.js'

export function validateFile(file) {
  if (!ACCEPTED_MIME_TYPES.includes(file.type)) return 'Only PNG, JPEG or PDF files are allowed.'
  if (file.size > MAX_UPLOAD_BYTES) return `File is larger than ${formatFileSize(MAX_UPLOAD_BYTES)}.`
  return null
}

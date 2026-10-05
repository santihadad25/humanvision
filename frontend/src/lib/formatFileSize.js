const BYTES_PER_KB = 1024
const BYTES_PER_MB = 1024 * 1024

export function formatFileSize(bytes) {
  if (bytes < BYTES_PER_MB) return `${Math.max(1, Math.round(bytes / BYTES_PER_KB))} KB`
  const megabytes = bytes / BYTES_PER_MB
  return `${Number.isInteger(megabytes) ? megabytes : megabytes.toFixed(1)} MB`
}

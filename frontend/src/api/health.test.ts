import { beforeEach, describe, expect, it, vi } from 'vitest'

const { getMock } = vi.hoisted(() => ({ getMock: vi.fn() }))

vi.mock('./http', () => ({
  default: { get: getMock },
}))

import { checkHealth } from './health'

describe('checkHealth', () => {
  beforeEach(() => {
    getMock.mockReset()
  })

  it('returns true for an ok response', async () => {
    getMock.mockResolvedValue({ data: { status: 'ok' } })

    await expect(checkHealth()).resolves.toBe(true)
    expect(getMock).toHaveBeenCalledWith('/api/health')
  })

  it('returns false for an unexpected response', async () => {
    getMock.mockResolvedValue({ data: { status: 'unknown' } })

    await expect(checkHealth()).resolves.toBe(false)
  })

  it('returns false when the backend is unavailable', async () => {
    getMock.mockRejectedValue(new Error('offline'))

    await expect(checkHealth()).resolves.toBe(false)
  })
})

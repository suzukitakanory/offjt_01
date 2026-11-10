export type AttendanceStatus = 'WORK' | 'PAID_LEAVE' | 'SICK_LEAVE' | 'UNPAID_LEAVE'

export interface AttendanceRecord {
  id: number
  employeeId: string
  attendanceDate: string
  clockInTime: string | null
  clockOutTime: string | null
  status: AttendanceStatus
  notes: string | null
  createdAt: string
  updatedAt: string
}

export interface WorkTimeRequest {
  employeeId: string
  attendanceDate: string
  clockInTime: string
  clockOutTime: string
  status: 'WORK'
}

export interface AttendanceApiErrorResponse {
  error: string
  message: string
  timestamp: string
}

// Keep the shared request shape for the separate leave-registration flow.
export interface AttendanceRequest {
  employeeId: string
  attendanceDate: string
  clockInTime?: string
  clockOutTime?: string
  status?: string
  notes?: string
}

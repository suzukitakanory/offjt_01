export const LEAVE_TYPES = ['Sick Leave', 'Personal Leave', 'Business Trip'] as const

export type LeaveType = (typeof LEAVE_TYPES)[number]

export interface LeaveRecord {
  date: string
  leaveType: LeaveType
  memo: string
}

export interface CreateLeaveRequest {
  date: string
  leaveType: LeaveType
  memo?: string
  confirmReplaceWork?: boolean
}

export interface UpdateLeaveRequest {
  leaveType: LeaveType
  memo?: string
}

export interface LeaveApiErrorResponse {
  error: string
  message: string
}

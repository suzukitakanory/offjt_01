import holidayCsv from './syukujitsu.csv?raw'

interface HolidayRecord {
  date: string
  name: string
}

const HEADER = '国民の祝日・休日月日,国民の祝日・休日名称'
const DATE_PATTERN = /^(\d{4})\/(\d{1,2})\/(\d{1,2})$/

const parseHolidayDate = (value: string, lineNumber: number): string => {
  const match = DATE_PATTERN.exec(value)
  if (!match) {
    throw new Error(`Invalid holiday date on CSV line ${lineNumber}: ${value}`)
  }

  const [, yearText, monthText, dayText] = match
  const year = Number(yearText)
  const month = Number(monthText)
  const day = Number(dayText)
  const date = new Date(Date.UTC(year, month - 1, day))

  if (
    date.getUTCFullYear() !== year ||
    date.getUTCMonth() !== month - 1 ||
    date.getUTCDate() !== day
  ) {
    throw new Error(`Invalid calendar date on CSV line ${lineNumber}: ${value}`)
  }

  return `${yearText}-${monthText.padStart(2, '0')}-${dayText.padStart(2, '0')}`
}

const parseHolidayCsv = (csv: string): HolidayRecord[] => {
  const lines = csv.replace(/^\uFEFF/, '').trimEnd().split(/\r?\n/)
  if (lines[0] !== HEADER) {
    throw new Error('Unexpected header in the Cabinet Office holiday CSV')
  }

  const records = new Map<string, HolidayRecord>()
  let previousDate: string | undefined

  for (const [index, line] of lines.slice(1).entries()) {
    const lineNumber = index + 2
    const separatorIndex = line.indexOf(',')
    if (separatorIndex < 0 || line.indexOf(',', separatorIndex + 1) >= 0) {
      throw new Error(`Invalid columns on holiday CSV line ${lineNumber}`)
    }

    const date = parseHolidayDate(line.slice(0, separatorIndex), lineNumber)
    const name = line.slice(separatorIndex + 1).trim()
    if (!name) {
      throw new Error(`Missing holiday name on CSV line ${lineNumber}`)
    }
    if (records.has(date)) {
      throw new Error(`Duplicate holiday date on CSV line ${lineNumber}: ${date}`)
    }
    if (previousDate && date <= previousDate) {
      throw new Error(`Holiday dates are not in ascending order on CSV line ${lineNumber}`)
    }

    records.set(date, { date, name })
    previousDate = date
  }

  if (records.size === 0) {
    throw new Error('The Cabinet Office holiday CSV contains no holiday records')
  }

  return [...records.values()]
}

const holidayRecords = parseHolidayCsv(holidayCsv)
const holidayNames = new Map(holidayRecords.map(({ date, name }) => [date, name]))

export const HOLIDAY_DATA_RANGE = {
  startDate: holidayRecords[0].date,
  endDate: holidayRecords[holidayRecords.length - 1].date,
}

export const getHolidayName = (date: string): string | undefined => holidayNames.get(date)

export const isWithinHolidayDataRange = (date: string): boolean =>
  date >= HOLIDAY_DATA_RANGE.startDate && date <= HOLIDAY_DATA_RANGE.endDate

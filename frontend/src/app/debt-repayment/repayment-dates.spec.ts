import { asDebt, lastDayOfMonth, parseMonth } from './repayment-dates';

describe('repayment dates', () => {
  it('parses YYYY-MM into year and zero-based month', () => {
    expect(parseMonth('2026-10')).toEqual({ year: 2026, month: 9 });
  });

  it('returns the last day of a month, including leap years and year rollover', () => {
    expect(lastDayOfMonth(2026, 9)).toEqual(new Date(2026, 9, 31));
    expect(lastDayOfMonth(2026, 10)).toEqual(new Date(2026, 10, 30));
    expect(lastDayOfMonth(2028, 1)).toEqual(new Date(2028, 1, 29));
    expect(lastDayOfMonth(2026, 12)).toEqual(new Date(2027, 0, 31));
  });

  it('presents debt as a negative amount without a negative zero', () => {
    expect(asDebt(100000)).toBe(-100000);
    expect(Object.is(asDebt(0), 0)).toBe(true);
  });
});

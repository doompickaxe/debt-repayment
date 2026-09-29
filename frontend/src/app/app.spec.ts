import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { App } from './app';
import { DebtRepaymentResponse, ErrorResponse, provideApi } from './api';

const response: DebtRepaymentResponse = {
  parameters: { loanAmount: 100000, annualInterestRate: 2.12, loanPeriodYears: 10, initialRepaymentRate: 2 },
  schedule: [
    { month: 1, remainingDebt: 99833.34, interest: 176.67, repayment: 166.66, instalment: 343.33 },
    { month: 2, remainingDebt: 99666.38, interest: 176.37, repayment: 166.96, instalment: 343.33 },
  ],
  summary: { remainingDebt: 99666.38, totalInterest: 353.04, totalRepayment: 333.62, totalInstalments: 686.66 },
};

describe('App', () => {
  let http: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [App],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideApi('/api')],
    }).compileComponents();
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  async function render() {
    const fixture = TestBed.createComponent(App);
    await fixture.whenStable();
    return { fixture, element: fixture.nativeElement as HTMLElement };
  }

  function setValue(element: HTMLElement, selector: string, value: string) {
    const input = element.querySelector<HTMLInputElement>(selector)!;
    input.value = value;
    input.dispatchEvent(new Event('input'));
  }

  function submit(element: HTMLElement) {
    element.querySelector<HTMLButtonElement>('button[type=submit]')!.click();
  }

  it('sends the entered parameters and shows the dated schedule with negative debt', async () => {
    const { fixture, element } = await render();

    setValue(element, '#payoutMonth', '2026-10');
    submit(element);
    const request = http.expectOne({ method: 'POST', url: '/api/debt-repayment' });
    expect(request.request.body).toEqual(response.parameters);
    request.flush(response);
    await fixture.whenStable();

    const rows = [...element.querySelectorAll('tbody tr')].map((row) =>
      [...row.querySelectorAll('td')].map((cell) => cell.textContent?.trim()),
    );
    expect(rows).toEqual([
      ['Oct 1, 2026', '-€100,000.00', '–', '–', '–'],
      ['Oct 31, 2026', '-€99,833.34', '€176.67', '€166.66', '€343.33'],
      ['Nov 30, 2026', '-€99,666.38', '€176.37', '€166.96', '€343.33'],
    ]);

    const summary = element.querySelector('dl')?.textContent;
    expect(summary).toContain('-€99,666.38');
    expect(summary).toContain('€686.66');
  });

  it('shows the error message returned by the backend', async () => {
    const { fixture, element } = await render();

    submit(element);
    const error: ErrorResponse = { errorCode: 'VALIDATION_ERROR', message: 'loanAmount: must be greater than or equal to 0.01' };
    http.expectOne('/api/debt-repayment').flush(error, { status: 400, statusText: 'Bad Request' });
    await fixture.whenStable();

    expect(element.querySelector('[data-slot=alert]')?.textContent).toContain(error.message);
    expect(element.querySelector('table')).toBeNull();
  });

  it('does not call the backend while the input is invalid', async () => {
    const { fixture, element } = await render();

    setValue(element, '#loanAmount', '');
    submit(element);
    await fixture.whenStable();

    http.expectNone('/api/debt-repayment');
    expect(element.textContent).toContain('Please enter the loan amount.');
  });
});

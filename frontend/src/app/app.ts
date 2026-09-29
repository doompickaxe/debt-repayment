import { CurrencyPipe, DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { HlmAlertImports } from '@spartan-ng/helm/alert';
import { HlmCardImports } from '@spartan-ng/helm/card';
import { finalize } from 'rxjs';
import { DebtRepaymentResponse, DebtRepaymentService, ErrorResponse } from './api';
import { DebtRepaymentForm, LoanCalculation } from './debt-repayment/debt-repayment-form';
import { parseMonth } from './debt-repayment/repayment-dates';
import { RepaymentSummaryView } from './debt-repayment/repayment-summary';
import { RepaymentTable } from './debt-repayment/repayment-table';

@Component({
  imports: [CurrencyPipe, DatePipe, HlmAlertImports, HlmCardImports, DebtRepaymentForm, RepaymentSummaryView, RepaymentTable],
  selector: 'app-root',
  templateUrl: './app.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class App {
  private readonly api = inject(DebtRepaymentService);
  private readonly destroyRef = inject(DestroyRef);

  protected readonly loading = signal(false);
  protected readonly result = signal<{ response: DebtRepaymentResponse; payoutMonth: string } | undefined>(undefined);
  protected readonly error = signal<string | undefined>(undefined);

  protected payoutDate(payoutMonth: string): Date {
    const { year, month } = parseMonth(payoutMonth);
    return new Date(year, month, 1);
  }

  protected calculate({ request, payoutMonth }: LoanCalculation): void {
    this.loading.set(true);
    this.error.set(undefined);

    this.api
      .calculateDebtRepayment(request)
      .pipe(
        finalize(() => this.loading.set(false)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: (response) => this.result.set({ response, payoutMonth }),
        error: (e: HttpErrorResponse) => {
          this.result.set(undefined);
          this.error.set(toErrorMessage(e));
        },
      });
  }
}

function toErrorMessage(e: HttpErrorResponse): string {
  const body = e.error as Partial<ErrorResponse> | null;
  if (e.status === 400 && body?.message) {
    return body.message;
  }
  if (e.status === 0) {
    return 'The server could not be reached. Please try again later.';
  }
  return `Unexpected error (${e.status}). Please try again later.`;
}

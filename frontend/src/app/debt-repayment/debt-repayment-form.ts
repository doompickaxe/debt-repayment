import { ChangeDetectionStrategy, Component, inject, input, output } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { HlmButtonImports } from '@spartan-ng/helm/button';
import { HlmFieldImports } from '@spartan-ng/helm/field';
import { HlmInputImports } from '@spartan-ng/helm/input';
import { HlmInputGroupImports } from '@spartan-ng/helm/input-group';
import { HlmSpinnerImports } from '@spartan-ng/helm/spinner';
import { DebtRepaymentRequest } from '../api';

/// Loan parameters for the backend plus the month of the payout, which is only used to date the schedule.
export interface LoanCalculation {
  request: DebtRepaymentRequest;
  /// Month of the payout as `YYYY-MM`
  payoutMonth: string;
}

/// Input for the loan parameters. Validation mirrors the constraints of the OpenAPI spec.
@Component({
  selector: 'app-debt-repayment-form',
  imports: [ReactiveFormsModule, HlmButtonImports, HlmFieldImports, HlmInputImports, HlmInputGroupImports, HlmSpinnerImports],
  templateUrl: './debt-repayment-form.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DebtRepaymentForm {
  readonly loading = input(false);
  readonly calculate = output<LoanCalculation>();

  protected readonly form = inject(NonNullableFormBuilder).group({
    loanAmount: [100000, [Validators.required, Validators.min(0.01)]],
    payoutMonth: [currentMonth(), [Validators.required, Validators.pattern(/^\d{4}-\d{2}$/)]],
    annualInterestRate: [2.12, [Validators.required, Validators.min(0)]],
    loanPeriodYears: [10, [Validators.required, Validators.min(1), Validators.max(100), Validators.pattern(/^\d+$/)]],
    initialRepaymentRate: [2, [Validators.required, Validators.min(0.01)]],
  });

  protected submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const { payoutMonth, ...request } = this.form.getRawValue();
    this.calculate.emit({ request, payoutMonth });
  }
}

function currentMonth(): string {
  const today = new Date();
  return `${today.getFullYear()}-${String(today.getMonth() + 1).padStart(2, '0')}`;
}

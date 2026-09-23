import { ChangeDetectorRef, Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent {
  cpf: string = '';
  senha: string = '';
  loading: boolean = false;
  errorMessage: string = '';

  constructor(private authService: AuthService, private router: Router, private cdr: ChangeDetectorRef) {}

  onSubmit(): void {
    if (!this.cpf) {
      this.errorMessage = 'Por favor, informe o CPF.';
      return;
    }

    this.loading = true;
    this.errorMessage = '';

    this.authService.login({ cpf: this.cpf, senha: this.senha }).subscribe({
      next: (res) => {
        this.loading = false;
        if (res.cargo === 'GESTOR') {
          this.router.navigate(['/gestor']);
        } else if (res.cargo === 'FISCAL') {
          this.router.navigate(['/fiscal']);
        } else {
          this.errorMessage = 'Cargo de usuário não reconhecido.';
        }
        this.cdr.markForCheck();
      },
      error: (err) => {
        this.loading = false;
        this.errorMessage = err.error?.message || 'Falha na autenticação. Verifique o CPF e a senha.';
        this.cdr.markForCheck();
      }
    });
  }

  preencherDemo(cpf: string, label: string): void {
    this.cpf = cpf;
    this.senha = 'Demo@123';
    this.errorMessage = '';
  }
}

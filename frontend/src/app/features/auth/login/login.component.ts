import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [ReactiveFormsModule, CommonModule, RouterLink],
  template: `
    <div style="min-height:100vh;display:flex;align-items:center;justify-content:center;padding:24px;background:radial-gradient(ellipse at top,#1a1f35 0%,var(--bg) 70%)">
      <div style="width:100%;max-width:420px">

        <div style="text-align:center;margin-bottom:40px">
          <div style="font-size:40px;margin-bottom:12px">⚡</div>
          <h1 style="font-size:28px;font-weight:800;background:linear-gradient(135deg,var(--primary-lt),var(--accent));-webkit-background-clip:text;-webkit-text-fill-color:transparent">
            EventCollab
          </h1>
          <p style="color:var(--text-muted);margin-top:8px">Connectez-vous a votre compte</p>
        </div>

        <div class="card">
          @if (error) {
            <div class="alert alert-error">{{ error }}</div>
          }

          <form [formGroup]="form" (ngSubmit)="submit()">
            <div class="form-group">
              <label>Email</label>
              <input class="form-control" type="email" formControlName="email" placeholder="vous@exemple.com" autocomplete="email">
            </div>
            <div class="form-group">
              <label>Mot de passe</label>
              <input class="form-control" type="password" formControlName="password" placeholder="Votre mot de passe" autocomplete="current-password">
            </div>
            <button class="btn btn-primary btn-lg" style="width:100%;margin-top:8px" type="submit" [disabled]="loading">
              {{ loading ? "Connexion..." : "Se connecter" }}
            </button>
          </form>
        </div>

        <p style="text-align:center;margin-top:20px;font-size:14px;color:var(--text-muted)">
          Pas encore de compte ?
          <a routerLink="/auth/register" style="color:var(--primary-lt);font-weight:500;text-decoration:none"> Creer un compte</a>
        </p>
      </div>
    </div>
  `
})
export class LoginComponent {
  form: FormGroup;
  error = '';
  loading = false;

  constructor(private fb: FormBuilder, private auth: AuthService, private router: Router) {
    this.form = this.fb.group({
      email:    ['', [Validators.required, Validators.email]],
      password: ['', Validators.required]
    });
  }

  submit() {
    if (this.form.invalid) return;
    this.loading = true; this.error = '';
    this.auth.login(this.form.value).subscribe({
      next: () => this.router.navigate(['/events']),
      error: err => { this.error = err.error?.message || "Email ou mot de passe incorrect"; this.loading = false; }
    });
  }
}
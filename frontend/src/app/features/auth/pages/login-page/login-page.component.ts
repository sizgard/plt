import {ChangeDetectionStrategy, Component, inject, signal} from '@angular/core';
import {AuthService} from "../../services/auth.service";
import {Router} from "@angular/router";
import {LoginRequest} from "../../models/auth.models";
import {HttpErrorResponse} from "@angular/common/http";
import {LoginFormComponent} from "../../components/login-form/login-form.component";

@Component({
  selector: 'app-login-page',
  standalone: true,
  imports: [
    LoginFormComponent
  ],
  templateUrl: './login-page.component.html',
  styleUrl: './login-page.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class LoginPageComponent {
  private readonly authService = inject(AuthService)
  private readonly router = inject(Router)

  readonly isLoading = signal(false)
  readonly errorMessage = signal<string | null>(null);

  onLogin(req: LoginRequest): void{
    this.isLoading.set(true)
    this.errorMessage.set(null)
    this.authService.login(req).subscribe({
      next: () =>{
        this.isLoading.set(false)
        this.router.navigate(['/']);
      },
      error: (err: HttpErrorResponse)=>{
        this.isLoading.set(false)
        if(err.status === 401){
          this.errorMessage.set('Неверный email или пароль')
        }else {
          this.errorMessage.set('Произошла ошибка при входе. Попробуйте позже.')
        }
      }
    })
  }
}

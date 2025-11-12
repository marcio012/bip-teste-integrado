import {Component, OnInit} from '@angular/core';
import {FormsModule} from '@angular/forms';
import {ActivatedRoute, Router} from '@angular/router';
import axios from 'axios';
import {environment} from '../../../environments/environment';

@Component({
  selector: 'app-beneficio-form',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './beneficio-form.html',
  styleUrls: ['./beneficio-form.scss'],
})
export class BeneficioFormComponent implements OnInit {
  id?: number;
  form = { nome: '', descricao: '', valor: 0, ativo: true };
  loading = false;
  error = '';

  private base = `${environment.apiBase}`;

  constructor(private route: ActivatedRoute, private router: Router) {}

  ngOnInit() {
    const param = this.route.snapshot.paramMap.get('id');
    if (param) {
      this.id = +param;
      this.loading = true;
      axios.get(`${this.base}/${this.id}`)
        .then(r => this.form = r.data)
        .catch(e => this.error = e?.response?.data || 'Erro ao carregar')
        .finally(() => this.loading = false);
    }
  }

  salvar() {
    this.loading = true;
    this.error = '';
    const op = this.id
      ? axios.put(`${this.base}/${this.id}`, this.form)
      : axios.post(`${this.base}`, this.form);

    op.then(() => this.router.navigate(['/beneficios']))
      .catch(e => this.error = e?.response?.data || 'Erro ao salvar')
      .finally(() => this.loading = false);
  }

  voltar() { this.router.navigate(['/beneficios']); }
}

import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import axios from 'axios';
import { environment } from '../../../environments/environment';

@Component({
  selector: 'app-transferencia-form',
  standalone: true,
  imports: [ FormsModule],
  templateUrl: './transferencia-form.html',
  styleUrls: ['./transferencia-form.scss'],
})
export class TransferenciaFormComponent {
  origemId?: number;
  destinoId?: number;
  valor?: number;
  msg = '';
  loading = false;

  private base = `${environment.apiBase}`;

  transferir() {
    if (this.origemId == null || this.destinoId == null || this.valor == null) return;
    this.loading = true;
    this.msg = '';
    axios.post(`${this.base}/transferencias`, null, {
      params: { origemId: this.origemId, destinoId: this.destinoId, valor: this.valor }
    }).then(() => this.msg = 'Transferência realizada')
      .catch(e => this.msg = e?.response?.data || 'Erro ao transferir')
      .finally(() => this.loading = false);
  }
}

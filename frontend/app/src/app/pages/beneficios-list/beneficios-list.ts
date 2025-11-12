import {Component, OnInit} from '@angular/core';
import {DecimalPipe} from '@angular/common';
import {RouterLink} from '@angular/router';
import axios from 'axios';
import {environment} from '../../../environments/environment';
import {Beneficio} from '../../services/beneficios';

@Component({
  selector: 'app-beneficios-list',
  standalone: true,
  imports: [DecimalPipe, RouterLink],
  templateUrl: './beneficios-list.html',
  styleUrls: ['./beneficios-list.scss'],
})
export class BeneficiosListComponent implements OnInit {
  itens: Beneficio[] = [];
  loading = false;
  error = '';

  private base = `${environment.apiBase}`;

  ngOnInit() { this.reload(); }

  reload() {
    this.loading = true;
    this.error = '';
    axios.get<Beneficio[]>(this.base)
      .then(r => this.itens = r.data)
      .catch(e => this.error = e?.response?.data || 'Erro ao carregar')
      .finally(() => this.loading = false);
  }

  excluir(b: Beneficio) {
    if (!b.id) return;
    if (confirm(`Excluir ${b.nome}?`)) {
      axios.delete(`${this.base}/${b.id}`)
        .then(() => this.reload())
        .catch(e => this.error = e?.response?.data || 'Erro ao excluir');
    }
  }
}

import {Injectable} from '@angular/core';
import axios from 'axios';
import {environment} from '../../environments/environment';

export interface Beneficio {
  id?: number;
  nome: string;
  descricao?: string;
  valor: number;
  ativo: boolean;
  version?: number;
}

@Injectable({providedIn: 'root'})
export class BeneficiosService {
  private base = environment.apiBase;

    list() {
    return axios.get<Beneficio[]>(`${this.base}`);
  }

  get(id: number) {
    return axios.get<Beneficio>(`${this.base}/${id}`);
  }

  create(b: Beneficio) {
    return axios.post<Beneficio>(`${this.base}`, b);
  }

  update(id: number, b: Beneficio) {
    return axios.put<Beneficio>(`${this.base}/${id}`, b);
  }

  delete(id: number) {
    return axios.delete(`${this.base}/${id}`);
  }

  creditar(id: number, valor: number) {
    return axios.post(`${this.base}/${id}/creditos`, null, {params: {valor}});
  }

  debitar(id: number, valor: number) {
    return axios.post(`${this.base}/${id}/debitos`, null, {params: {valor}});
  }

  transferir(origemId: number, destinoId: number, valor: number) {
    return axios.post(`${this.base}/transferencias`, null, {params: {origemId, destinoId, valor}});
  }
}

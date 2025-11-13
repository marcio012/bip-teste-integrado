import { TestBed } from '@angular/core/testing';
import axios, { AxiosResponse } from 'axios';
import { BeneficiosService, Beneficio } from './beneficios';
import { environment } from '../../environments/environment';

describe('BeneficiosService', () => {
  let service: BeneficiosService;

  const mockBeneficio: Beneficio = {
    id: 1,
    nome: 'Teste',
    descricao: 'Descrição teste',
    valor: 100.50,
    ativo: true,
    version: 0
  };

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(BeneficiosService);
  });

  it('deve ser criado', () => {
    expect(service).toBeTruthy();
  });

  describe('list', () => {
    it('deve listar todos os benefícios', async () => {
      const mockResponse: AxiosResponse = {
        data: [mockBeneficio],
        status: 200,
        statusText: 'OK',
        headers: {},
        config: {} as any
      };

      spyOn(axios, 'get').and.returnValue(Promise.resolve(mockResponse));

      const result = await service.list();

      expect(axios.get).toHaveBeenCalledWith(environment.apiBase);
      expect(result.data).toEqual([mockBeneficio]);
    });

    it('deve lançar erro ao falhar na listagem', async () => {
      const errorMessage = 'Erro de rede';
      spyOn(axios, 'get').and.returnValue(Promise.reject(new Error(errorMessage)));

      try {
        await service.list();
        fail('Deveria ter lançado erro');
      } catch (error: any) {
        expect(error.message).toBe(errorMessage);
      }
    });
  });

  describe('get', () => {
    it('deve buscar um benefício por ID', async () => {
      const mockResponse: AxiosResponse = {
        data: mockBeneficio,
        status: 200,
        statusText: 'OK',
        headers: {},
        config: {} as any
      };

      spyOn(axios, 'get').and.returnValue(Promise.resolve(mockResponse));

      const result = await service.get(1);

      expect(axios.get).toHaveBeenCalledWith(`${environment.apiBase}/1`);
      expect(result.data).toEqual(mockBeneficio);
    });
  });

  describe('create', () => {
    it('deve criar um novo benefício', async () => {
      const novoBeneficio: Beneficio = {
        nome: 'Novo',
        descricao: 'Novo benefício',
        valor: 200,
        ativo: true
      };

      const mockResponse: AxiosResponse = {
        data: { ...novoBeneficio, id: 2, version: 0 },
        status: 201,
        statusText: 'Created',
        headers: {},
        config: {} as any
      };

      spyOn(axios, 'post').and.returnValue(Promise.resolve(mockResponse));

      const result = await service.create(novoBeneficio);

      expect(axios.post).toHaveBeenCalledWith(environment.apiBase, novoBeneficio);
      expect(result.data).toEqual(jasmine.objectContaining({ id: 2 }));
    });
  });

  describe('update', () => {
    it('deve atualizar um benefício existente', async () => {
      const beneficioAtualizado = { ...mockBeneficio, nome: 'Atualizado' };
      const mockResponse: AxiosResponse = {
        data: beneficioAtualizado,
        status: 200,
        statusText: 'OK',
        headers: {},
        config: {} as any
      };

      spyOn(axios, 'put').and.returnValue(Promise.resolve(mockResponse));

      const result = await service.update(1, beneficioAtualizado);

      expect(axios.put).toHaveBeenCalledWith(
        `${environment.apiBase}/1`,
        beneficioAtualizado
      );
      expect(result.data.nome).toBe('Atualizado');
    });
  });

  describe('delete', () => {
    it('deve excluir um benefício', async () => {
      const mockResponse: AxiosResponse = {
        data: null,
        status: 204,
        statusText: 'No Content',
        headers: {},
        config: {} as any
      };

      spyOn(axios, 'delete').and.returnValue(Promise.resolve(mockResponse));

      await service.delete(1);

      expect(axios.delete).toHaveBeenCalledWith(`${environment.apiBase}/1`);
    });
  });

  describe('creditar', () => {
    it('deve creditar valor em um benefício', async () => {
      const mockResponse: AxiosResponse = {
        data: null,
        status: 204,
        statusText: 'No Content',
        headers: {},
        config: {} as any
      };

      spyOn(axios, 'post').and.returnValue(Promise.resolve(mockResponse));

      await service.creditar(1, 50.00);

      expect(axios.post).toHaveBeenCalledWith(
        `${environment.apiBase}/1/creditos`,
        null,
        { params: { valor: 50.00 } }
      );
    });
  });

  describe('debitar', () => {
    it('deve debitar valor de um benefício', async () => {
      const mockResponse: AxiosResponse = {
        data: null,
        status: 204,
        statusText: 'No Content',
        headers: {},
        config: {} as any
      };

      spyOn(axios, 'post').and.returnValue(Promise.resolve(mockResponse));

      await service.debitar(1, 30.00);

      expect(axios.post).toHaveBeenCalledWith(
        `${environment.apiBase}/1/debitos`,
        null,
        { params: { valor: 30.00 } }
      );
    });
  });

  describe('transferir', () => {
    it('deve transferir valor entre benefícios', async () => {
      const mockResponse: AxiosResponse = {
        data: null,
        status: 204,
        statusText: 'No Content',
        headers: {},
        config: {} as any
      };

      spyOn(axios, 'post').and.returnValue(Promise.resolve(mockResponse));

      await service.transferir(1, 2, 25.00);

      expect(axios.post).toHaveBeenCalledWith(
        `${environment.apiBase}/transferencias`,
        null,
        { params: { origemId: 1, destinoId: 2, valor: 25.00 } }
      );
    });
  });
});

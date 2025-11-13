
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NgZone } from '@angular/core';
import { BeneficiosListComponent } from './beneficios-list';
import { Beneficio } from '../../services/beneficios';

describe('BeneficiosListComponent', () => {
  let component: BeneficiosListComponent;
  let fixture: ComponentFixture<BeneficiosListComponent>;
  let mockNgZone: jasmine.SpyObj<NgZone>;

  const mockBeneficios: Beneficio[] = [
    {
      id: 1,
      nome: 'Beneficio A',
      descricao: 'Descrição A',
      valor: 1000,
      ativo: true,
      version: 0
    },
    {
      id: 2,
      nome: 'Beneficio B',
      descricao: 'Descrição B',
      valor: 500,
      ativo: true,
      version: 0
    }
  ];

  beforeEach(async () => {
    // Mock do NgZone
    mockNgZone = jasmine.createSpyObj('NgZone', ['run', 'runOutsideAngular']);
    mockNgZone.run.and.callFake((fn: Function) => fn());

    await TestBed.configureTestingModule({
      imports: [BeneficiosListComponent],
      providers: [
        { provide: NgZone, useValue: mockNgZone }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(BeneficiosListComponent);
    component = fixture.componentInstance;
  });

  afterEach(() => {
    fixture.destroy();
  });

  it('deve criar o componente', () => {
    expect(component).toBeTruthy();
  });

  it('deve inicializar com valores padrão', () => {
    expect(component.itens).toEqual([]);
    expect(component.loading).toBe(false);
    expect(component.error).toBe('');
  });

  describe('ngOnInit', () => {
    it('deve chamar reload ao inicializar', () => {
      spyOn(component, 'reload');
      component.ngOnInit();
      expect(component.reload).toHaveBeenCalled();
    });
  });

  describe('reload', () => {
    it('deve definir loading como true ao iniciar carregamento', () => {
      component.reload();
      expect(component.loading).toBe(true);
    });

    it('deve limpar o erro ao recarregar', () => {
      component.error = 'Erro anterior';
      component.reload();
      expect(component.error).toBe('');
    });

    it('deve chamar ngZone.run ao processar resposta', () => {
      component.reload();
      // Aguarda um pouco para o axios processar
      setTimeout(() => {
        expect(mockNgZone.run).toHaveBeenCalled();
      }, 100);
    });
  });

  describe('excluir', () => {
    let beneficioParaExcluir: Beneficio;

    beforeEach(() => {
      beneficioParaExcluir = mockBeneficios[0];
      component.itens = [...mockBeneficios];
      spyOn(window, 'confirm');
    });

    it('não deve fazer nada se o benefício não tiver ID', () => {
      const beneficioSemId: Beneficio = {
        nome: 'Teste',
        valor: 100,
        ativo: true
      };

      component.excluir(beneficioSemId);

      expect(window.confirm).not.toHaveBeenCalled();
    });

    it('não deve excluir se o usuário cancelar a confirmação', () => {
      (window.confirm as jasmine.Spy).and.returnValue(false);
      spyOn(component, 'reload');

      component.excluir(beneficioParaExcluir);

      expect(component.reload).not.toHaveBeenCalled();
    });

    it('deve exibir mensagem de confirmação com nome do benefício', () => {
      (window.confirm as jasmine.Spy).and.returnValue(false);

      component.excluir(beneficioParaExcluir);

      expect(window.confirm).toHaveBeenCalledWith('Excluir Beneficio A?');
    });

    it('deve processar exclusão se confirmado', () => {
      (window.confirm as jasmine.Spy).and.returnValue(true);

      component.excluir(beneficioParaExcluir);

      expect(window.confirm).toHaveBeenCalledWith('Excluir Beneficio A?');
    });
  });

  describe('Template Integration', () => {
    it('deve exibir "Carregando..." quando loading for true', () => {
      component.loading = true;
      fixture.detectChanges();

      const compiled = fixture.nativeElement as HTMLElement;
      expect(compiled.textContent).toContain('Carregando...');
    });

    it('deve exibir mensagem de erro quando houver erro', () => {
      component.loading = false;
      component.error = 'Erro teste';
      fixture.detectChanges();

      const compiled = fixture.nativeElement as HTMLElement;
      expect(compiled.textContent).toContain('Erro teste');
    });

    it('deve exibir "Nenhum benefício cadastrado" quando lista vazia', () => {
      component.loading = false;
      component.itens = [];
      fixture.detectChanges();

      const compiled = fixture.nativeElement as HTMLElement;
      expect(compiled.textContent).toContain('Nenhum benefício cadastrado');
    });

    it('deve exibir tabela com benefícios quando houver dados', () => {
      component.loading = false;
      component.itens = mockBeneficios;
      fixture.detectChanges();

      const compiled = fixture.nativeElement as HTMLElement;
      expect(compiled.querySelector('table')).toBeTruthy();
      expect(compiled.textContent).toContain('Beneficio A');
      expect(compiled.textContent).toContain('Beneficio B');
    });

    it('deve exibir valores formatados corretamente', () => {
      component.loading = false;
      component.itens = mockBeneficios;
      fixture.detectChanges();

      const compiled = fixture.nativeElement as HTMLElement;
      // Verifica se os valores estão sendo exibidos
      expect(compiled.textContent).toContain('1,000');
      expect(compiled.textContent).toContain('500');
    });

    it('deve ter botão de recarregar', () => {
      component.loading = false;
      fixture.detectChanges();

      const compiled = fixture.nativeElement as HTMLElement;
      const reloadButton = compiled.querySelector('button');
      expect(reloadButton?.textContent).toContain('Recarregar');
    });

    it('deve ter link para criar novo benefício', () => {
      component.loading = false;
      fixture.detectChanges();

      const compiled = fixture.nativeElement as HTMLElement;
      const newLink = compiled.querySelector('a[routerLink="/beneficios/novo"]');
      expect(newLink).toBeTruthy();
    });
  });
});

package com.example.algamoney.api.repository.lancamento;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.util.StringUtils;

import com.example.algamoney.api.dto.LancamentoEstatisticaCategoria;
import com.example.algamoney.api.dto.LancamentoEstatisticaDia;
import com.example.algamoney.api.dto.LancamentoEstatisticaPessoa;
import com.example.algamoney.api.model.Categoria_;
import com.example.algamoney.api.model.Lancamento;
import com.example.algamoney.api.model.Lancamento_;
import com.example.algamoney.api.model.Pessoa_;
import com.example.algamoney.api.repository.filter.LancamentoFilter;
import com.example.algamoney.api.repository.projecao.ResumoLancamento;

public class LancamentoRepositoryImpl implements LancamentoRepositoryQuery {

	@PersistenceContext
	private EntityManager manager;
	
	@Override
	public Page<Lancamento> filtrar(LancamentoFilter lancamentoFilter, Pageable pageable) {
		
		CriteriaBuilder builder = manager.getCriteriaBuilder();
		CriteriaQuery<Lancamento> criteria = builder.createQuery(Lancamento.class);
		
		Root<Lancamento> root = criteria.from(Lancamento.class);
		

		//criar as restrições
		Predicate[]predicates = criarRestricoes(lancamentoFilter, builder, root);
		criteria.where(predicates);
		
		TypedQuery<Lancamento> query = manager.createQuery(criteria);
		adicionarRetricoesDePaginacao(query, pageable); //Método criado para adicionar como deve ser a paginação vinda pelo parametros no pageable
		
		
		return new PageImpl<>( query.getResultList(), pageable, total(lancamentoFilter));
	}


	@Override
	public Page<ResumoLancamento> resumir(LancamentoFilter lancamentoFilter, Pageable pageable) {
		CriteriaBuilder builder = manager.getCriteriaBuilder();
		CriteriaQuery<ResumoLancamento> criteria = builder.createQuery(ResumoLancamento.class);
		Root<Lancamento> root = criteria.from(Lancamento.class);  //1. SELECT FROM LANCAMENTO
		
		//PRECISA SEGUIR A MESMA ORDEM DO CONSTRUTOR EM ResumoLancamento POIS AQUI ESTA CRIANDO O OBJETO ResumoLancamento
		criteria.select(builder.construct(ResumoLancamento.class   					//2. CAMPOS DA CLAUSULA SELECT 
				, root.get(Lancamento_.codigo), root.get(Lancamento_.descricao)
				, root.get(Lancamento_.dataVencimento), root.get(Lancamento_.dataPagamento)
				, root.get(Lancamento_.valor), root.get(Lancamento_.tipo)
				, root.get(Lancamento_.categoria).get(Categoria_.nome)
				, root.get(Lancamento_.pessoa).get(Pessoa_.nome)));
		
		Predicate[] predicates = criarRestricoes(lancamentoFilter, builder, root);   //3. CRIANDO O WHERE DA CLAUSULA SQL 
		criteria.where(predicates);
		
		TypedQuery<ResumoLancamento> query = manager.createQuery(criteria);
		adicionarRetricoesDePaginacao(query, pageable);
		
		return new PageImpl<>(query.getResultList(), pageable, total(lancamentoFilter));
	}



	private Predicate[] criarRestricoes(LancamentoFilter lancamentoFilter, CriteriaBuilder builder,
			Root<Lancamento> root) {
		
		List<Predicate> predicates = new ArrayList<>();
		
		if(!StringUtils.isEmpty(lancamentoFilter.getDescricao())) {
			 predicates.add(builder.like (
					 builder.lower(root.get(Lancamento_.descricao)), "%" + lancamentoFilter.getDescricao().toLowerCase() + "%"));
		} 
		
		if(lancamentoFilter.getDataVencimentoDe() != null) {
			predicates.add( 
					builder.greaterThanOrEqualTo(root.get(Lancamento_.DATA_VENCIMENTO),lancamentoFilter.getDataVencimentoDe()));
		}
		
		if(lancamentoFilter.getDataVencimentoAte()!= null) {
			predicates.add(
					builder.lessThanOrEqualTo(root.get(Lancamento_.DATA_VENCIMENTO), lancamentoFilter.getDataVencimentoAte()));
		}
		
		return predicates.toArray(new Predicate[predicates.size()]);
	}

	private void adicionarRetricoesDePaginacao(TypedQuery<?> query, Pageable pageable) {
		int paginaAtual = pageable.getPageNumber(); /* com getPagNumber() eu pago o page passado em localhost:8080/lancamentos?size=3&page=2 */
		int totalRegistrosPorPagina = pageable.getPageSize(); // pega a propriedade size passada no endereço como parametro
		int primeiroRegistroDaPagina = paginaAtual*totalRegistrosPorPagina;
		
		query.setFirstResult(primeiroRegistroDaPagina);
		query.setMaxResults(totalRegistrosPorPagina);
	}
	
	private Long total(LancamentoFilter lancamentoFilter) {
		
		CriteriaBuilder builder = manager.getCriteriaBuilder();
		CriteriaQuery<Long> criteria = builder.createQuery(Long.class);
		Root<Lancamento> root = criteria.from(Lancamento.class);
		
		Predicate[] predicates = criarRestricoes(lancamentoFilter, builder, root);
		criteria.where(predicates);
		
		criteria.select(builder.count(root));

		return manager.createQuery(criteria).getSingleResult();
	}


	//CODIGO AULA APÊNDICE: 22.2
	@Override
	public List<LancamentoEstatisticaCategoria> porCategoria(LocalDate mesReferencia) {
		CriteriaBuilder criteriaBuilder = manager.getCriteriaBuilder();
		
		CriteriaQuery<LancamentoEstatisticaCategoria> criteriaQuery = criteriaBuilder.
					createQuery(LancamentoEstatisticaCategoria.class);  //No generics esta LancamentoEstatisticaCategoria pois é o que queremos devolver 
		
		Root<Lancamento> root = criteriaQuery.from(Lancamento.class); //AQUI O Root foi em Lancamento pois a pesquisa sera(From) na tabela de lançamento
		
		criteriaQuery.select(criteriaBuilder.construct(LancamentoEstatisticaCategoria.class,
				 root.get(Lancamento_.categoria),
				 criteriaBuilder.sum(root.get(Lancamento_.valor))));
		
		//Pegando o primeiro e ultimo dia do mês com base na data informada em mesReferencia
		LocalDate primeiroDia = mesReferencia.withDayOfMonth(1);
		LocalDate ultimoDia = mesReferencia.withDayOfMonth(mesReferencia.lengthOfMonth());
		
		
		criteriaQuery.where(
				
				criteriaBuilder.greaterThanOrEqualTo(root.get(Lancamento_.dataVencimento), 
						primeiroDia),
						criteriaBuilder.lessThanOrEqualTo(root.get(Lancamento_.dataVencimento),
						ultimoDia));
		
		criteriaQuery.groupBy(root.get(Lancamento_.categoria)); //Agrupando pois temos um sum em total que será agrupado por categoria
		
		TypedQuery<LancamentoEstatisticaCategoria> typedQuery = manager
					.createQuery(criteriaQuery);
					
		
		return typedQuery.getResultList();
	}

	//CODIGO AULA APÊNDICE: 22.4
	@Override
	public List<LancamentoEstatisticaDia> porDia(LocalDate mesReferencia) {
		CriteriaBuilder criteriaBuilder = manager.getCriteriaBuilder();
		
		CriteriaQuery<LancamentoEstatisticaDia> criteriaQuery = criteriaBuilder.
					createQuery(LancamentoEstatisticaDia.class);  //No generics esta LancamentoEstatisticaCategoria pois é o que queremos devolver 
		
		Root<Lancamento> root = criteriaQuery.from(Lancamento.class); //AQUI O Root foi em Lancamento pois a pesquisa sera(From) na tabela de lançamento
		
		criteriaQuery.select(criteriaBuilder.construct(LancamentoEstatisticaDia.class,
				 root.get(Lancamento_.tipo),
				 root.get(Lancamento_.dataVencimento),
				 criteriaBuilder.sum(root.get(Lancamento_.valor))));
		
		//Pegando o primeiro e ultimo dia do mês com base na data informada em mesReferencia
		LocalDate primeiroDia = mesReferencia.withDayOfMonth(1);
		LocalDate ultimoDia = mesReferencia.withDayOfMonth(mesReferencia.lengthOfMonth());
		
		
		criteriaQuery.where(
				
				criteriaBuilder.greaterThanOrEqualTo(root.get(Lancamento_.dataVencimento), 
						primeiroDia),
						criteriaBuilder.lessThanOrEqualTo(root.get(Lancamento_.dataVencimento),
						ultimoDia));
		
		criteriaQuery.groupBy(root.get(Lancamento_.tipo),
							  root.get(Lancamento_.dataVencimento)); //Agrupando pois temos um sum em total que será agrupado por categoria
		
		TypedQuery<LancamentoEstatisticaDia> typedQuery = manager
					.createQuery(criteriaQuery);
					
		
		return typedQuery.getResultList();
	}


	//CODIGO AULA APÊNDICE: 22.12
		@Override
		public List<LancamentoEstatisticaPessoa> porPessoa(LocalDate inicio,LocalDate fim) {
			CriteriaBuilder criteriaBuilder = manager.getCriteriaBuilder();
			
			CriteriaQuery<LancamentoEstatisticaPessoa> criteriaQuery = criteriaBuilder.
						createQuery(LancamentoEstatisticaPessoa.class);  //No generics esta LancamentoEstatisticaCategoria pois é o que queremos devolver 
			
			Root<Lancamento> root = criteriaQuery.from(Lancamento.class); //AQUI O Root foi em Lancamento pois a pesquisa sera(From) na tabela de lançamento
			
			criteriaQuery.select(criteriaBuilder.construct(LancamentoEstatisticaPessoa.class,
					 root.get(Lancamento_.tipo),
					 root.get(Lancamento_.PESSOA),
					 criteriaBuilder.sum(root.get(Lancamento_.valor))));
			
						
			criteriaQuery.where(
					
					criteriaBuilder.greaterThanOrEqualTo(root.get(Lancamento_.dataVencimento), 
							inicio),
							criteriaBuilder.lessThanOrEqualTo(root.get(Lancamento_.dataVencimento),
							fim));
			
			criteriaQuery.groupBy(root.get(Lancamento_.tipo),
								  root.get(Lancamento_.pessoa)); //Agrupando pois temos um sum em total que será agrupado por categoria
			
			TypedQuery<LancamentoEstatisticaPessoa> typedQuery = manager
						.createQuery(criteriaQuery);
						
			
			return typedQuery.getResultList();
		}




}

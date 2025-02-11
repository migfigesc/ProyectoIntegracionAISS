package aiss.api.resources;


import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.jboss.resteasy.spi.BadRequestException;
import org.jboss.resteasy.spi.NotFoundException;

import aiss.api.resources.comparators.ComparatorTituloLista;
import aiss.api.resources.comparators.ComparatorTituloListaReversed;
import aiss.model.Lista;
import aiss.model.Tarea;
import aiss.model.repository.ListaRepository;
import aiss.model.repository.MapListaRepository;


@Path("/lista")
public class ListaResource {
	
	/* Singleton */
	private static ListaResource _instance=null;
	ListaRepository repository;
	
	public ListaResource() {
		repository=MapListaRepository.getInstance();
	}
	
	public static ListaResource getInstance()
	{
		if(_instance==null)
				_instance=new ListaResource();
		return _instance;
	}

	@GET
	@Produces("application/json")
	public Collection<Lista> getAll(@QueryParam("order") String order,@QueryParam("isEmpty") Boolean isEmpty,
		@QueryParam("titulo") String titulo,@QueryParam("limit") Integer limit,@QueryParam("offset") Integer offset){
		
		List<Lista> result= new ArrayList<Lista>();
		if(offset==(null) || offset>repository.getAllListas().size()) {
			offset=0;
		}
		
		if(limit==(null) || limit>repository.getAllListas().size()) {
			limit=repository.getAllListas().size();
		}
		
		int i=0;
		int j=0;

		for(Lista lista: repository.getAllListas()) {
			if(j>=offset && i<limit) {
				if(titulo == null || lista.getTitulo().contains(titulo) ) {
					if(isEmpty== null
						|| (isEmpty && (lista.getTareas() == null || lista.getTareas().size() == 0))
						|| (!isEmpty && (lista.getTareas() != null || lista.getTareas().size() > 0))){
						i++;
						result.add(lista);
					}
				}
			}
			j++;
		}

		if(order!=null) {
			if(order.equals("titulo")) {
				Collections.sort(result, new ComparatorTituloLista());
			}
			else if(order.equals("-titulo")) {
				Collections.sort(result, new ComparatorTituloListaReversed());
			}
			else {
				throw new BadRequestException("El parámetro para ordenar debe ser 'titulo'.");
			}
		}	
		return result;
	}
	
	@GET
	@Path("/{id}")
	@Produces("application/json")
	public Lista get(@PathParam("id") String id){
		Lista list = repository.getLista(id);
		if (list == null) {
			throw new NotFoundException("No se ha encontrado la lista con id ="+ id);			
		}
		return list;
	}
	
	@POST
	@Consumes("application/json")
	@Produces("application/json")
	public Response addLista(@Context UriInfo uriInfo, Lista lista) {
		if (lista.getTitulo() == null || "".equals(lista.getTitulo()))
			throw new BadRequestException("El Titulo de la lista no puede ser nulo.");
		if (lista.getTareas()!=null)
			throw new BadRequestException("Las tareas de la lista no se pueden editar.");
		repository.addLista(lista);
		// Builds the response. Returns the playlist the has just been added.
		UriBuilder ub = uriInfo.getAbsolutePathBuilder().path(this.getClass(), "get");
		URI uri = ub.build(lista.getId());
		ResponseBuilder resp = Response.created(uri);
		resp.entity(lista);			
		return resp.build();
	}
	
	@PUT
	@Consumes("application/json")
	public Response updateLista(Lista lista) {
		Lista oldLista = repository.getLista(lista.getId());
		if (oldLista == null) {
			throw new NotFoundException("La lista con id ="+ lista.getId() +" no se ha encontrado.");			
		}
		if (lista.getTareas()!=null)
			throw new BadRequestException("Las tareas de la lista no son editables.");
		// Update name
		if (lista.getTitulo()!=null)
			oldLista.setTitulo(lista.getTitulo());
		// Update description
		if (lista.getDescripcion()!=null)
			oldLista.setDescripcion(lista.getDescripcion());
		return Response.noContent().build();
	}
	
	@DELETE
	@Path("/{id}")
	public Response removeLista(@PathParam("id") String id) {
		Lista toberemoved=repository.getLista(id);
		if (toberemoved == null)
			throw new NotFoundException("La lista con id="+ id +" no se encuentra");
		else
			repository.deleteLista(id);
		return Response.noContent().build();
	}
	
	@POST	
	@Path("/{listaid}/{tareaid}")
	@Consumes("text/plain")
	@Produces("application/json")
	public Response addTarea(@Context UriInfo uriInfo,@PathParam("listaid") String listaid, @PathParam("tareaid") String tareaid){
		
		Lista lista = repository.getLista(listaid);
		Tarea tarea = repository.getTarea(tareaid);
		if (lista==null)
			throw new NotFoundException("La lista con id=" + listaid + " no se encuentra");
		if (tarea == null)
			throw new NotFoundException("La tarea con id=" + tareaid + " no se encuentra");
		if (lista.getTarea(tareaid)!=null)
			throw new BadRequestException("La tarea ya está incluida en la lista");
		
		// Cada vez que se añade una tarea se comprueba si la lista esta completada
//		lista.setCompletado(true);
//		for(Tarea tareaAuxiliar:lista.getTareas()) {
//			if(tareaAuxiliar.getCompletado()==false) {
//				lista.setCompletado(false);
//			}
//		}
			
		repository.addTarea(listaid, tareaid);		
		// Builds the response
		UriBuilder ub = uriInfo.getAbsolutePathBuilder().path(this.getClass(), "get");
		URI uri = ub.build(listaid);
		ResponseBuilder resp = Response.created(uri);
		resp.entity(lista);			
		return resp.build();
	}
	
	
	@DELETE
	@Path("/{listaid}/{tareaid}")
	public Response removeTarea(@PathParam("listaid") String listaid, @PathParam("tareaid") String tareaid) {
		Lista lista = repository.getLista(listaid);
		Tarea tarea = repository.getTarea(tareaid);
		
		if (lista==null)
			throw new NotFoundException("La lista con id =" + listaid + " no se encuentra");
		
		if (tarea == null)
			throw new NotFoundException("La tarea con id =" + tareaid + " no se encuentra");
		
		
		repository.removeTarea(listaid, tareaid);		
		
		return Response.noContent().build();
	}

	public boolean addTarea(String listaId, String tareaId) {
		// TODO Auto-generated method stub
		return false;
	}
}
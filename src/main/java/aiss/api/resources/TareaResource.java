package aiss.api.resources;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.jboss.resteasy.spi.BadRequestException;
import org.jboss.resteasy.spi.NotFoundException;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import aiss.api.resources.comparators.ComparatorCategoriaTarea;
import aiss.api.resources.comparators.ComparatorFechaCreacionTarea;
import aiss.api.resources.comparators.ComparatorFechaVencimientoTarea;
import aiss.api.resources.comparators.ComparatorTituloTarea;
import aiss.model.Tarea;
import aiss.model.repository.ListaRepository;
import aiss.model.repository.MapListaRepository;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;


@Path("/songs")
public class TareaResource {

	public static TareaResource _instance=null;
	ListaRepository repository;
	
	private TareaResource(){
		repository=MapListaRepository.getInstance();
	}
	
	public static TareaResource getInstance()
	{
		if(_instance==null)
			_instance=new TareaResource();
		return _instance; 
	}
	
	@GET
	@Produces("application/json")
	public Collection<Tarea> getAll(@QueryParam("order") String order,@QueryParam("isEmpty") Boolean isEmpty,
			@QueryParam("q") String q,@QueryParam("limit") Integer limit,@QueryParam("offset") Integer offset )
	{
		List<Tarea> result= new ArrayList<Tarea>();
		
		if(offset==(null) || offset>repository.getAllTareas().size()) {
			offset=0;
		}
		if(limit==(null) || limit>repository.getAllTareas().size()) {
			limit=repository.getAllTareas().size();
		}
		
		int i=0;
		int j=0;
		for(Tarea tarea : repository.getAllTareas()) {
			if(j>=offset && i<limit) {
				if( q == null || tarea.getTitulo().contains(q) || tarea.getCategoria().contains(q) || tarea.getDescripcion().contains(q) ){
					i++;
					result.add(tarea);
				}
			}
			j++;
		}
		
		if(order!=null) {
			if(order.equals("titulo")) {
				Collections.sort(result, new ComparatorTituloTarea());
			}else if(order.equals("fv")) {
				Collections.sort(result, new ComparatorFechaVencimientoTarea());
			}else if(order.equals("fc")) {
				Collections.sort(result, new ComparatorFechaCreacionTarea());
			}else if(order.equals("cat")) {
				Collections.sort(result, new ComparatorCategoriaTarea());
			}else {
				throw new BadRequestException("The order parameter must be a 'titulo' or 'fv' or 'fc' or 'cat'.");
			}
		}
		return result;
	}
	
	
	@GET
	@Path("/{id}")
	@Produces("application/json")
	public Tarea get(@PathParam("id") String tareaId){
		Tarea list = repository.getTarea(tareaId);
		if (list == null) {
			throw new NotFoundException("La tarea con el id= "+ tareaId +" no ha sido encontrado");			
		}
		
		return list;
	}
	
	@POST
	@Consumes("application/json")
	@Produces("application/json")
	public Response addTarea(@Context UriInfo uriInfo, Tarea tarea) {
		if (tarea.getTitulo() == null || "".equals(tarea.getTitulo()))
			throw new BadRequestException("El nombre de la tarea no puede ser nulo.");

		repository.addTarea(tarea);

		// Builds the response. Returns the playlist the has just been added.
		UriBuilder ub = uriInfo.getAbsolutePathBuilder().path(this.getClass(), "get");
		URI uri = ub.build(tarea.getId());
		ResponseBuilder resp = Response.created(uri);
		resp.entity(tarea);			
		return resp.build();
	}
	
	@PUT
	@Consumes("application/json")
	public Response updateTarea(Tarea tarea) {
		Tarea oldTarea = repository.getTarea(tarea.getId());
		if (oldTarea == null) {
			throw new NotFoundException("La tarea con el id= "+ tarea.getId() +" no ha sido encontrado.");			
		}
		
		// Update title
		if (tarea.getTitulo()!=null)
			oldTarea.setTitulo(tarea.getTitulo());
		
		// Update Descripcion
		if (tarea.getDescripcion()!=null)
			oldTarea.setDescripcion(tarea.getDescripcion());
		
		//Update Categoría
		if (tarea.getCategoria()!=null)
			oldTarea.setCategoria(tarea.getCategoria());
		
		//Update Completado
		if (tarea.getCompletado()!=null)
			oldTarea.setCompletado(tarea.getCompletado());
		
		//Update FechaCreacion
		if (tarea.getFechaCreacion()!=null)
			oldTarea.setFechaCreacion(tarea.getFechaCreacion());
		
		//Update FechaVencimiento
				if (tarea.getFechaVencimiento()!=null)
					oldTarea.setFechaVencimiento(tarea.getFechaVencimiento());
		
		return Response.noContent().build();
	}
	
	@DELETE
	@Path("/{id}")
	public Response removeTarea(@PathParam("id") String tareaId) {
		
		Tarea toberemoved=repository.getTarea(tareaId);
		if (toberemoved == null)
			throw new NotFoundException("La tarea con el id="+ tareaId +" no ha sido encontrada.");
		else
			repository.deleteTarea(tareaId);
		
		return Response.noContent().build();
	}
	
}

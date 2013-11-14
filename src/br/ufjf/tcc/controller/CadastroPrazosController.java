package br.ufjf.tcc.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.Init;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Window;

import br.ufjf.tcc.business.CalendarioSemestreBusiness;
import br.ufjf.tcc.business.PrazoBusiness;
import br.ufjf.tcc.model.CalendarioSemestre;
import br.ufjf.tcc.model.Prazo;

public class CadastroPrazosController extends CommonsController {
	private CalendarioSemestre calendar;
	private int currentPrazo = -1;
	private List<Prazo> prazos = new ArrayList<Prazo>();
	private List<Integer> types = new ArrayList<Integer>();
	private boolean editing = false;

	@Init
	public void init(
			@ExecutionArgParam("calendar") CalendarioSemestre calendar,
			@ExecutionArgParam("editing") boolean editing) {
		this.calendar = calendar;
		this.editing = editing;

		for (int i = 0; i < 4; i++)
			types.add(i);

		if (this.editing) {
			DateTime currentDay = new DateTime(new Date());

			prazos = calendar.getPrazos();

			for (int i = prazos.size() - 1; i >= 0; i--)
				if (currentDay.isAfter(new DateTime(prazos.get(i)
						.getDataFinal()))) {
					currentPrazo = i + 1;
					break;
				}
		} else {
			DateTime finalDate = new DateTime(this.calendar.getFinalSemestre());

			for (int i = 0; i < 4; i++) {

				Prazo aux = new Prazo();
				aux.setCalendarioSemestre(this.calendar);
				aux.setTipo(i);
				switch (i) {
				case Prazo.ENTREGA_FORM_BANCA:
					aux.setDataFinal(finalDate.minusDays(22).toDate());
					break;
				case Prazo.ENTREGA_TCC_BANCA:
					aux.setDataFinal(finalDate.minusDays(22).toDate());
					break;
				case Prazo.DEFESA:
					aux.setDataFinal(finalDate.minusDays(7).toDate());
					break;
				case Prazo.ENTREGA_FINAL:
					aux.setDataFinal(finalDate.toDate());
					break;
				}
				prazos.add(aux);
			}
		}
	}

	public List<Prazo> getPrazos() {
		return prazos;
	}

	public void setPrazos(List<Prazo> prazos) {
		this.prazos = prazos;
	}

	public List<Integer> getTypes() {
		return types;
	}

	public void setTypes(List<Integer> types) {
		this.types = types;
	}

	public CalendarioSemestre getCalendar() {
		return calendar;
	}

	public void setCalendar(CalendarioSemestre calendar) {
		this.calendar = calendar;
	}

	public int getCurrentPrazo() {
		return currentPrazo;
	}

	public void setCurrentPrazo(int currentPrazo) {
		this.currentPrazo = currentPrazo;
	}

	public boolean isEditing() {
		return editing;
	}
	
	@Command
	public void goBack(@BindingParam("window") Window window){
		window.detach();
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("calendar", calendar);
		final Window dialog = (Window) Executions.createComponents(
				"/pages/cadastro-calendario.zul", null, map);
		dialog.doModal();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Command
	public void submit(@BindingParam("window") Window window) {
		if (editing) {
			if (!new PrazoBusiness().editList(prazos)) {
				Messagebox.show("Não foi possível salvar o calendário",
						"Erro", Messagebox.OK, Messagebox.ERROR);
				return;
			}
		} else {
			calendar.setPrazos(prazos);
			if (new CalendarioSemestreBusiness().save(calendar)) {
				if (!new PrazoBusiness().saveList(prazos)) {
					Messagebox.show("Não foi possível salvar o calendário",
							"Erro", Messagebox.OK, Messagebox.ERROR);
					return;
				}
			} else {
				Messagebox.show("Não foi possível salvar o calendário", "Erro",
						Messagebox.OK, Messagebox.ERROR);
				return;
			}
		}

		Messagebox.show("Calendário cadastrado com sucesso.", "Concluído",
				Messagebox.OK, Messagebox.INFORMATION, new EventListener() {
		    public void onEvent(Event evt) throws InterruptedException {
		    	Executions.sendRedirect("/pages/home-professor.zul");
		    }
		});
		window.detach();		
	}

	@Command
	public void getDescription(@BindingParam("comboitem") Comboitem comboitem,
			@BindingParam("combobox") Combobox combobox) {
		if (comboitem != null)
			comboitem.setLabel(new PrazoBusiness().getDescription(comboitem
					.getIndex()));
		if (combobox != null)
			combobox.setValue(new PrazoBusiness().getDescription(combobox
					.getSelectedIndex()));
	}

}

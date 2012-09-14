package com.anthavio.spring.web;

/**
 * @author vanek
 *
 * Controller Get:
 * 
 * ArrayList<IntOption> ret = new ArrayList<IntOption>();
 * ret.add(new IntOption(null, "..."));
 * ret.add(new IntOption(1, "Leden"));
 * ...
 * model.addAttribute("MesiceInterval", ret);
 * 
 * Jsp:
 * 
 * <sf:select path='mesice' cssErrorClass="error">
 *	<sf:options items='${MesiceInterval}' itemValue='value' itemLabel='label'/>
 * </sf:select>
 * 
 * Controller Post:
 * Predpokladejme ze se mapuje do form beany (command) jmenem criteria
 * pak property criteria.mesice typu Integer bude nasetovana na cislo, nebo null
 */
public class IntOption {

	private Integer value;

	private String label;

	public IntOption() {
		//default
	}

	public IntOption(Integer value, String label) {
		super();
		this.value = value;
		this.label = label;
	}

	public Integer getValue() {
		return value;
	}

	public void setValue(Integer value) {
		this.value = value;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

}

package com.anthavio.spring.web;

import org.codehaus.jackson.map.AnnotationIntrospector;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.introspect.JacksonAnnotationIntrospector;
import org.codehaus.jackson.xc.JaxbAnnotationIntrospector;

public class JacksonMapperBuilder {

	/**
	 * It is hard to configure Jackson's ObjectMapper AnnotationIntrospectors in spring xml,
	 * so here comes builder method
	 */
	public static ObjectMapper buildMapper(boolean jackson, boolean jaxb) {
		AnnotationIntrospector introspector = null;
		if (jackson && jaxb) {
			AnnotationIntrospector primary = new JacksonAnnotationIntrospector();
			AnnotationIntrospector secondary = new JaxbAnnotationIntrospector();
			introspector = new AnnotationIntrospector.Pair(primary, secondary);
		} else if (jackson) {
			introspector = new JacksonAnnotationIntrospector();
		} else if (jaxb) {
			introspector = new JaxbAnnotationIntrospector();
		}

		ObjectMapper mapper = new ObjectMapper();
		if (introspector == null) {
			mapper.getDeserializationConfig().with(org.codehaus.jackson.map.DeserializationConfig.Feature.USE_ANNOTATIONS);
			mapper.getSerializationConfig().with(org.codehaus.jackson.map.SerializationConfig.Feature.USE_ANNOTATIONS);
		} else {
			mapper.getDeserializationConfig().withAnnotationIntrospector(introspector);
			mapper.getSerializationConfig().withAnnotationIntrospector(introspector);
		}
		return mapper;
	}
}

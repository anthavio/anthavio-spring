package com.anthavio.xml;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.NamespaceContext;

import com.sun.xml.bind.marshaller.NamespacePrefixMapper;

/**
 * @author vanek
 * 
 * Prevod namespace uri <-> prefix
 * 
 * Implementuje jednak JAXB {@link NamespacePrefixMapper} a jednak JAXP {@link NamespaceContext}
 *
 */
public class NamespaceMapper extends NamespacePrefixMapper implements NamespaceContext {

	private final HashMap<String, String> uri2prefix = new HashMap<String, String>();

	private final HashMap<String, String> prefix2uri = new HashMap<String, String>();

	private final Iterator<String> EMPTY = new EmptyIterator();

	public NamespaceMapper() {
		//default mappings
		prefix2uri.put("xsi", "http://www.w3.org/2001/XMLSchema-instance");
		prefix2uri.put("xsd", "http://www.w3.org/2001/XMLSchema");

		Set<String> prefixSet = prefix2uri.keySet();
		for (String prefix : prefixSet) {
			uri2prefix.put(prefix2uri.get(prefix), prefix);
		}
	}

	/**
	 * Set configuration map <util:map>
	 */
	public void setPefixToUriMap(Map<String, String> map) {
		Set<String> prefixSet = map.keySet();
		for (String prefix : prefixSet) {
			prefix2uri.put(prefix, map.get(prefix));
			uri2prefix.put(map.get(prefix), prefix);
		}
	}

	public void setUriToPefixMap(Map<String, String> map) {
		Set<String> uriSet = map.keySet();
		for (String uri : uriSet) {
			prefix2uri.put(map.get(uri), uri);
			uri2prefix.put(uri, map.get(uri));
		}
	}

	public String getNamespaceURI(String prefix) {
		return prefix2uri.get(prefix);
	}

	public String getPrefix(String namespaceURI) {
		return prefix2uri.get(namespaceURI);
	}

	public Iterator<?> getPrefixes(String namespaceUri) {
		String prefix = uri2prefix.get(namespaceUri);
		return prefix == null ? EMPTY : new OneIterator(prefix);
	}

	@Override
	public String getPreferredPrefix(String namespaceUri, String suggestion, boolean requirePrefix) {
		String prefix = uri2prefix.get(namespaceUri);

		if (prefix != null) {
			if (requirePrefix) {
				return prefix;
			} else {
				return prefix;
			}
		} else {
			if (requirePrefix) {
				return suggestion;
			} else {
				return "";
			}
		}
		// return prefix != null && requirePrefix ? prefix : null;
	}

	private class EmptyIterator implements Iterator<String> {

		public boolean hasNext() {
			return false;
		}

		public String next() {
			return null;
		}

		public void remove() {
		}

	}

	private class OneIterator implements Iterator<String> {

		private final String string;

		private boolean taken;

		public OneIterator(String string) {
			this.string = string;
		}

		public boolean hasNext() {
			if (taken) {
				return false;
			} else {
				taken = true;
				return true;
			}
		}

		public String next() {
			return string;
		}

		public void remove() {
		}

	}
}

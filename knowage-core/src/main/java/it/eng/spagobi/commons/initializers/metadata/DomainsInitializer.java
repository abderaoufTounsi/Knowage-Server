/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.
 *
 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.eng.spagobi.commons.initializers.metadata;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;

import it.eng.spago.base.SourceBean;
import it.eng.spagobi.commons.metadata.SbiDomains;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public class DomainsInitializer extends SpagoBIInitializer {

	static private Logger logger = Logger.getLogger(DomainsInitializer.class);

	public DomainsInitializer() {
		targetComponentName = "Domains";
		configurationFileName = "it/eng/spagobi/commons/initializers/metadata/config/domains.xml";
	}

	@Override
	public void init(SourceBean config, Session hibernateSession) {
		logger.debug("IN");
		try {
			String hql = "from SbiDomains";
			Query hqlQuery = hibernateSession.createQuery(hql);
			List domains = hqlQuery.list();
			if (domains.isEmpty()) {
				logger.info("Domains table is empty. Starting populating domains...");
				writeDomains(hibernateSession);
			} else {
				logger.debug("Domains table is already populated, only missing domains will be populated");
				writeMissingDomains(hibernateSession);
			}
		} catch (Throwable t) {
			throw new SpagoBIRuntimeException("Ab unexpected error occured while initializeng Domains", t);
		} finally {
			logger.debug("OUT");
		}
	}

	private void writeDomains(Session aSession) throws Exception {
		logger.debug("IN");
		SourceBean domainsSB = getConfiguration();
		if (domainsSB == null) {
			throw new Exception("Domains configuration file not found!!!");
		}
		List domainsList = domainsSB.getAttributeAsList("DOMAIN");
		if (domainsList == null || domainsList.isEmpty()) {
			throw new Exception("No predefined domains found!!!");
		}
		Iterator it = domainsList.iterator();
		while (it.hasNext()) {
			SourceBean aDomainSB = (SourceBean) it.next();
			SbiDomains aDomain = new SbiDomains();
			aDomain.setDomainCd((String) aDomainSB.getAttribute("domainCd"));
			aDomain.setDomainNm((String) aDomainSB.getAttribute("domainNm"));
			aDomain.setValueCd((String) aDomainSB.getAttribute("valueCd"));
			aDomain.setValueNm((String) aDomainSB.getAttribute("valueNm"));
			aDomain.setValueDs((String) aDomainSB.getAttribute("valueDs"));
			logger.debug(
					"Inserting Domain with valueCd = [" + aDomainSB.getAttribute("valueCd") + "], domainCd = [" + aDomainSB.getAttribute("domainCd") + "] ...");
			aSession.save(aDomain);
		}
		logger.debug("OUT");
	}

	private void writeMissingDomains(Session aSession) throws Exception {
		logger.debug("IN");
		SourceBean domainsSB = getConfiguration();
		if (domainsSB == null) {
			throw new Exception("Domains configuration file not found!!!");
		}
		List domainsList = domainsSB.getAttributeAsList("DOMAIN");
		if (domainsList == null || domainsList.isEmpty()) {
			throw new Exception("No predefined domains found!!!");
		}

		List alreadyExamined = new ArrayList();
		Iterator it = domainsList.iterator();
		while (it.hasNext()) {
			SourceBean aDomainSB = (SourceBean) it.next();
			if (!alreadyExamined.contains(aDomainSB)) {

				String domainCd = (String) aDomainSB.getAttribute("domainCd");
				if (domainCd == null || domainCd.equals("")) {
					logger.error("No predefined domains code found!!!");
					throw new Exception("No predefined domains code found!!!");
				}
				// Retrieving all the domains in the DB with the specified domain Code
				logger.debug("Retrieving all the domains in the DB with the specified domain Code");
				String hql = "from SbiDomains where domainCd = :domainCd";
				Query hqlQuery = aSession.createQuery(hql);
				hqlQuery.setParameter("domainCd", domainCd);
				List result = hqlQuery.list();

				logger.debug("Retrieving all the domains in the XML file with the specified domain Code");
				// Retrieving all the domains in the XML file with the specified domain Code
				List domainsXmlList = domainsSB.getFilteredSourceBeanAttributeAsList("DOMAIN", "domainCd", domainCd);

				addMissingDomains(aSession, result, domainsXmlList);
				// Removing form the list of XML domains the ones already checked
				logger.debug("Adding to the list of XML domains already checked");
				alreadyExamined.addAll(domainsXmlList);
			}
		}
		logger.debug("OUT");
	}

	private void addMissingDomains(Session aSession, List dbDomains, List xmlDomains) {
		logger.debug("IN");

		Iterator it2 = xmlDomains.iterator();
		while (it2.hasNext()) {
			boolean existsInDb = false;
			SourceBean aDomainSB = (SourceBean) it2.next();
			String valueCdXml = (String) aDomainSB.getAttribute("valueCd");
			logger.debug("Retrieved valueCd of XML Domain: " + valueCdXml);

			Iterator it = dbDomains.iterator();
			while (it.hasNext()) {
				SbiDomains d = (SbiDomains) it.next();
				String valueCd = d.getValueCd();
				logger.debug("Retrieved valueCd of DB Domain: " + valueCd);

				if (valueCdXml.equalsIgnoreCase(valueCd)) {
					existsInDb = true;
					logger.debug("Domain already exists in the DB");

					d.setDomainNm((String) aDomainSB.getAttribute("domainNm"));
					d.setValueCd((String) aDomainSB.getAttribute("valueCd"));
					d.setValueNm((String) aDomainSB.getAttribute("valueNm"));
					d.setValueDs((String) aDomainSB.getAttribute("valueDs"));

					aSession.update(d);
					aSession.flush();
					break;
				}

			}
			if (!existsInDb) {
				logger.debug("Domain doesn't exist in the DB");
				SbiDomains aDomain = new SbiDomains();
				aDomain.setDomainCd((String) aDomainSB.getAttribute("domainCd"));
				aDomain.setDomainNm((String) aDomainSB.getAttribute("domainNm"));
				aDomain.setValueCd((String) aDomainSB.getAttribute("valueCd"));
				aDomain.setValueNm((String) aDomainSB.getAttribute("valueNm"));
				aDomain.setValueDs((String) aDomainSB.getAttribute("valueDs"));
				logger.debug("New Domain ready to be iserted in the DB");
				logger.debug("Inserting Domain with valueCd = [" + aDomainSB.getAttribute("valueCd") + "], domainCd = [" + aDomainSB.getAttribute("domainCd")
						+ "] ...");
				aSession.save(aDomain);
				aSession.flush();
				logger.debug("New Domain iserted in the DB");
			}
		}
		logger.debug("OUT");
	}

}

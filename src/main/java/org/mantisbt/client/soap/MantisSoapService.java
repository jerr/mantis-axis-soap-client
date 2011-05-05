package org.mantisbt.client.soap;
/**
 * MantisBT SOAP Client - stub for MantisBT soap WebService.
 * MantisBT SOAP Client is free software: you can redistribute
 * it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either
 * version 2 of the License, or (at your option) any later
 * version.
 * 
 * MantisBT SOAP Client is distributed in the hope that it will
 * be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with MantisBT.  If not, see <http://www.gnu.org/licenses/>.
 */

import java.math.BigInteger;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.rpc.ServiceException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import biz.futureware.mantis.rpc.soap.client.FilterData;
import biz.futureware.mantis.rpc.soap.client.IssueData;
import biz.futureware.mantis.rpc.soap.client.MantisConnectLocator;
import biz.futureware.mantis.rpc.soap.client.MantisConnectPortType;

/**
 * @author Jeremie Lagarde
 * @since 1.2.5
 */
public class MantisSoapService {

  private static final Logger LOG = LoggerFactory.getLogger(MantisSoapService.class);

  private MantisConnectPortType mantisConnectPortType;
  private String username;
  private String password;
  private BigInteger projectId;

  public BigInteger getProjectId() {
    if (projectId == null) {
      LOG.warn("Not connected yet! ProjecId isn't valid.");
    }
    return projectId;
  }

  public MantisSoapService(URL webServiceURL) throws RemoteException {
    MantisConnectLocator mantisConnectLocator = createMantisConnectLocator();
    try {
      if (webServiceURL == null) {
        mantisConnectPortType = mantisConnectLocator.getMantisConnectPort();
      } else {
        mantisConnectPortType = mantisConnectLocator.getMantisConnectPort(webServiceURL);
        LOG.debug("SOAP Session service endpoint at " + webServiceURL.toExternalForm());
      }
    } catch (ServiceException e) {
      throw new RemoteException("ServiceException during SOAPClient contruction", e);
    }
  }

  public void connect(String login, String password, String project) throws RemoteException {
    LOG.debug("Connnecting via SOAP as : {} for project : {}", login, project);
    this.username = login;
    this.password = password;
    projectId = mantisConnectPortType.mc_project_get_id_from_name(login, password, project);
    String version = mantisConnectPortType.mc_version();
    LOG.info("Connected in Mantis({})",version);
  }

  public FilterData[] getFilters() throws RemoteException {
    LOG.debug("Get filters via SOAP for : {}", getProjectId());
    return mantisConnectPortType.mc_filter_get(username, password, getProjectId());
  }

  public IssueData[] getIssues(FilterData filter) throws RemoteException {
    LOG.debug("Get issues via SOAP for {} : {}", getProjectId(), filter.getName());
    List<IssueData> issues = new ArrayList<IssueData>();
    IssueData[] result = null;
    int page = 1;
    do {
      result = mantisConnectPortType.mc_filter_get_issues(username, password, getProjectId(), filter.getId(), BigInteger.valueOf(page++),
          BigInteger.valueOf(100));
      issues.addAll(Arrays.asList(result));
    } while (result.length == 100);
    return issues.toArray(new IssueData[issues.size()]);
  }

  public void disconnect() throws RemoteException {
  }

  protected MantisConnectLocator createMantisConnectLocator() {
    return new MantisConnectLocator();
  }
}

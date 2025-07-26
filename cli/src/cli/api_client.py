"""API client for the K3s KVM Master API."""

# STL
import json
from typing import Any, Dict, Optional

# PDM
import requests
from requests import Response

# LOCAL
from .models import VMRequest, NodeRequest


class K3sAPIClient:
    """Client for interacting with the K3s KVM Master API."""

    def __init__(self, base_url: str = "http://127.0.0.1:8080") -> None:
        """Initialize the API client.

        Args:
            base_url: Base URL for the API server
        """
        self.base_url = base_url.rstrip("/")
        self.session = requests.Session()
        self.session.headers.update({"Content-Type": "application/json"})

    def _make_request(
        self,
        method: str,
        endpoint: str,
        data: Optional[Dict[str, Any]] = None,
        params: Optional[Dict[str, Any]] = None,
    ) -> Response:
        """Make an HTTP request to the API.

        Args:
            method: HTTP method (GET, POST, PUT, DELETE)
            endpoint: API endpoint path
            data: Request data for POST/PUT requests
            params: Query parameters for GET requests

        Returns:
            Response object from requests

        Raises:
            requests.RequestException: If the request fails
        """
        url = f"{self.base_url}{endpoint}"

        try:
            response = self.session.request(
                method=method, url=url, json=data, params=params
            )
            response.raise_for_status()
            return response
        except requests.RequestException as e:
            raise requests.RequestException(f"API request failed: {e}") from e

    def create_vm(self, vm_data: VMRequest) -> Dict[str, Any]:
        """Create a new VM.

        Args:
            vm_data: VM configuration data

        Returns:
            API response data
        """
        response = self._make_request(
            "POST", "/api/v1/vms", data=vm_data.model_dump(by_alias=True)
        )
        return response.json()

    def delete_vm(self, vm_name: str) -> Dict[str, Any]:
        """Delete a VM.

        Args:
            vm_name: Name of the VM to delete

        Returns:
            API response data
        """
        response = self._make_request("DELETE", f"/api/v1/vms/{vm_name}")
        return response.json()

    def list_vms(self) -> Dict[str, Any]:
        """List all VMs.

        Returns:
            API response data
        """
        response = self._make_request("GET", "/api/v1/vms")
        return response.json()

    def get_join_token(self) -> Dict[str, Any]:
        """Get the cluster join token.

        Returns:
            API response data
        """
        response = self._make_request("GET", "/api/v1/clusters/join-token")
        return response.json()

    def join_cluster(self, node_ip: str) -> Dict[str, Any]:
        """Join a node to the cluster.

        Args:
            node_ip: IP address of the node to join

        Returns:
            API response data
        """
        response = self._make_request(
            "GET", "/api/v1/clusters/join-cluster", params={"nodeIp": node_ip}
        )
        return response.json()

    def create_node(self, node_data: NodeRequest) -> Dict[str, Any]:
        """Create a new node.

        Args:
            node_data: Node configuration data

        Returns:
            API response data
        """
        response = self._make_request(
            "PUT", "/api/v1/clusters/node", data=node_data.model_dump(by_alias=True)
        )
        return response.json()

    def delete_node(self, node_name: str) -> Dict[str, Any]:
        """Delete a Node.

        Args:
            node_name: Name of the Node to delete

        Returns:
            API response data
        """
        response = self._make_request(
            "DELETE", f"/api/v1/clusters/node?nodeName={node_name}"
        )
        return response.json()

    def get_nodes(self) -> Dict[str, Any]:
        """Get all nodes

        Args:
            node_name: Name of the Node to delete

        Returns:
            API response data
        """
        response = self._make_request("GET", f"/api/v1/clusters/nodes")
        return response.json()

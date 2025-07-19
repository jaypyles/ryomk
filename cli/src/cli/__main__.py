"""Main CLI application for the K3s KVM Master API."""

# STL
import json
from typing import Optional

# PDM
import click

# LOCAL
from .models import VMRequest, NodeRequest
from .api_client import K3sAPIClient


def print_json(data: dict) -> None:
    """Pretty print JSON data.

    Args:
        data: Data to print
    """
    click.echo(json.dumps(data, indent=2))


@click.group()
@click.option(
    "--base-url", default="http://127.0.0.1:8080", help="Base URL for the API server"
)
@click.pass_context
def cli(ctx: click.Context, base_url: str) -> None:
    """K3s CLI tool for managing VMs and clusters."""
    ctx.ensure_object(dict)
    ctx.obj["client"] = K3sAPIClient(base_url)


@cli.group()
def vm() -> None:
    """Manage VMs."""
    pass


@vm.command("create")
@click.option("--name", required=True, help="Name of the VM")
@click.option("--ip-address", required=True, help="IP address of the VM")
@click.option("--gateway", required=True, help="Gateway IP address")
@click.option("--system-user", required=True, help="System user for the VM")
@click.option("--iso-path", required=True, help="Path to cloud-init ISO file")
@click.option("--vcpu", required=False, help="VCPU amount (1, 2, etc.)")
@click.option("--memory", required=False, help="Memory (in mb) ex: 2048")
def create_vm(
    name: str,
    ip_address: str,
    gateway: str,
    system_user: str,
    iso_path: str,
    vcpu: int,
    memory: int,
) -> None:
    """Create a new VM."""
    ctx = click.get_current_context()
    client: K3sAPIClient = ctx.obj["client"]

    try:
        vm_data = VMRequest(
            name=name,
            ip_address=ip_address,
            gateway=gateway,
            system_user=system_user,
            iso_path=iso_path,
            vcpu=vcpu,
            memory=memory,
        )

        result = client.create_vm(vm_data)
        print_json(result)
        click.echo("✅ VM created successfully")

    except Exception as e:
        click.echo(f"❌ Failed to create VM: {e}", err=True)
        raise click.Abort()


@vm.command("delete")
@click.argument("name")
def delete_vm(name: str) -> None:
    """Delete a VM."""
    ctx = click.get_current_context()
    client: K3sAPIClient = ctx.obj["client"]

    try:
        result = client.delete_vm(name)
        print_json(result)
        click.echo("✅ VM deleted successfully")

    except Exception as e:
        click.echo(f"❌ Failed to delete VM: {e}", err=True)
        raise click.Abort()


@vm.command("list")
def list_vms() -> None:
    """List all VMs."""
    ctx = click.get_current_context()
    client: K3sAPIClient = ctx.obj["client"]

    try:
        result = client.list_vms()
        print_json(result)

    except Exception as e:
        click.echo(f"❌ Failed to list VMs: {e}", err=True)
        raise click.Abort()


@cli.group()
def cluster() -> None:
    """Manage cluster operations."""
    pass


@cluster.command("create")
@click.option("--name", required=True, help="Name of the VM")
@click.option("--ip-address", required=True, help="IP address of the VM")
@click.option("--gateway", required=True, help="Gateway IP address")
@click.option("--system-user", required=True, help="System user for the VM")
@click.option("--iso-path", required=True, help="Path to cloud-init ISO file")
@click.option("--vcpu", required=False, help="VCPU amount (1, 2, etc.)")
@click.option("--memory", required=False, help="Memory (in mb) ex: 2048")
def create_vm(
    name: str,
    ip_address: str,
    gateway: str,
    system_user: str,
    iso_path: str,
    vcpu: int,
    memory: int,
) -> None:
    """Create a new VM."""
    ctx = click.get_current_context()
    client: K3sAPIClient = ctx.obj["client"]

    try:
        node_data = NodeRequest(
            name=name,
            ip_address=ip_address,
            gateway=gateway,
            system_user=system_user,
            iso_path=iso_path,
            vcpu=vcpu,
            memory=memory,
        )

        result = client.create_node(node_data)
        print_json(result)
        click.echo("✅ Node created successfully")

    except Exception as e:
        click.echo(f"❌ Failed to create Node: {e}", err=True)
        raise click.Abort()


@cluster.command("join-token")
def get_join_token() -> None:
    """Get the cluster join token."""
    ctx = click.get_current_context()
    client: K3sAPIClient = ctx.obj["client"]

    try:
        result = client.get_join_token()
        print_json(result)

    except Exception as e:
        click.echo(f"❌ Failed to get join token: {e}", err=True)
        raise click.Abort()


@cluster.command("join")
@click.option("--node-ip", required=True, help="IP address of the node to join")
def join_cluster(node_ip: str) -> None:
    """Join a node to the cluster."""
    ctx = click.get_current_context()
    client: K3sAPIClient = ctx.obj["client"]

    try:
        result = client.join_cluster(node_ip)
        print_json(result)
        click.echo("✅ Node joined cluster successfully")

    except Exception as e:
        click.echo(f"❌ Failed to join node to cluster: {e}", err=True)
        raise click.Abort()


@cluster.command("delete-pod")
@click.option("--pod-name", required=True, help="Name of the pod to delete")
def delete_pod(pod_name: str) -> None:
    """Delete a pod from the cluster."""
    ctx = click.get_current_context()
    client: K3sAPIClient = ctx.obj["client"]

    try:
        result = client.delete_pod(pod_name)
        print_json(result)
        click.echo("✅ Pod deleted successfully")

    except Exception as e:
        click.echo(f"❌ Failed to delete pod: {e}", err=True)
        raise click.Abort()


@cluster.command("delete")
@click.argument("name")
def delete_node(name: str) -> None:
    """Delete a Node."""
    ctx = click.get_current_context()
    client: K3sAPIClient = ctx.obj["client"]

    try:
        result = client.delete_node(name)
        print_json(result)
        click.echo("✅ Node deleted successfully")

    except Exception as e:
        click.echo(f"❌ Failed to delete Node: {e}", err=True)
        raise click.Abort()


@cluster.command("update-node")
@click.option("--name", required=True, help="Name of the node")
@click.option("--ip-address", required=True, help="IP address of the node")
@click.option("--gateway", required=True, help="Gateway IP address")
@click.option("--system-user", required=True, help="System user for the node")
@click.option("--iso-path", required=True, help="Path to cloud-init ISO file")
def update_node(
    name: str,
    ip_address: str,
    gateway: str,
    system_user: str,
    iso_path: str,
) -> None:
    """Update a node in the cluster."""
    ctx = click.get_current_context()
    client: K3sAPIClient = ctx.obj["client"]

    try:
        node_data = NodeRequest(
            name=name,
            ip_address=ip_address,
            gateway=gateway,
            system_user=system_user,
            iso_path=iso_path,
        )

        result = client.update_node(node_data)
        print_json(result)
        click.echo("✅ Node updated successfully")

    except Exception as e:
        click.echo(f"❌ Failed to update node: {e}", err=True)
        raise click.Abort()


def main() -> None:
    """Main entry point for the CLI."""
    cli()


if __name__ == "__main__":
    main()

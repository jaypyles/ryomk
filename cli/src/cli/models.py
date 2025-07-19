"""Data models for the K3s CLI tool."""

# STL
from typing import Optional

# PDM
from pydantic import Field, BaseModel


class VMRequest(BaseModel):
    """Model for VM creation/update requests."""

    name: str = Field(..., description="Name of the VM")
    ip_address: str = Field(..., alias="ipAddress", description="IP address of the VM")
    gateway: str = Field(..., description="Gateway IP address")
    system_user: str = Field(
        ..., alias="systemUser", description="System user for the VM"
    )
    iso_path: str = Field(
        ..., alias="isoPath", description="Path to cloud-init ISO file"
    )

    class Config:
        populate_by_name = True


class NodeRequest(BaseModel):
    """Model for node join requests."""

    name: str = Field(..., description="Name of the node")
    ip_address: str = Field(
        ..., alias="ipAddress", description="IP address of the node"
    )
    gateway: str = Field(..., description="Gateway IP address")
    system_user: str = Field(
        ..., alias="systemUser", description="System user for the node"
    )
    iso_path: str = Field(
        ..., alias="isoPath", description="Path to cloud-init ISO file"
    )

    class Config:
        populate_by_name = True


class APIResponse(BaseModel):
    """Generic API response model."""

    success: bool = Field(..., description="Whether the request was successful")
    message: Optional[str] = Field(None, description="Response message")
    data: Optional[dict] = Field(None, description="Response data")

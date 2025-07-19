package com.k3skvmmaster.util;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.libvirt.Connect;
import org.libvirt.Domain;
import org.libvirt.LibvirtException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.k3skvmmaster.model.dto.VmRequest;

@Component
public class LibvirtUtil {

    @Autowired
    private Connect libvirtConnect;

    public String generateDomainXml(VmRequest request, String cloudInitIsoPath) {
        // Generate VM domain XML using template
        return String.format("""
                <domain type='kvm' xmlns:qemu='http://libvirt.org/schemas/domain/qemu/1.0'>
                  <name>%s</name>
                  <memory unit='MiB'>%d</memory>
                  <vcpu placement='static'>%d</vcpu>
                  <os>
                    <type arch='x86_64' machine='pc-q35-5.2'>hvm</type>
                    <boot dev='hd'/>
                  </os>
                  <features>
                    <acpi/>
                    <apic/>
                  </features>
                  <cpu mode='host-model' check='partial'/>
                  <clock offset='utc'>
                    <timer name='rtc' tickpolicy='catchup'/>
                    <timer name='pit' tickpolicy='delay'/>
                    <timer name='hpet' present='no'/>
                  </clock>
                  <pm>
                    <suspend-to-mem enabled='no'/>
                    <suspend-to-disk enabled='no'/>
                  </pm>
                  <devices>
                    <emulator>/usr/bin/qemu-system-x86_64</emulator>
                    <disk type='file' device='disk'>
                      <driver name='qemu' type='qcow2'/>
                      <source file='/var/lib/libvirt/images/%s-disk.qcow2'/>
                      <target dev='vda' bus='virtio'/>
                    </disk>
                    <disk type='file' device='cdrom'>
                      <driver name='qemu' type='raw'/>
                      <source file='%s'/>
                      <target dev='sda' bus='sata'/>
                      <readonly/>
                    </disk>
                    <interface type='network'>
                      <source network='kube-net'/>
                      <model type='virtio'/>
                    </interface>
                    <serial type='pty'>
                      <target type='isa-serial' port='0'>
                        <model name='isa-serial'/>
                      </target>
                    </serial>
                    <console type='pty'>
                      <target type='serial' port='0'/>
                    </console>
                    <channel type='unix'>
                      <target type='virtio' name='org.qemu.guest_agent.0'/>
                    </channel>
                    <input type='tablet' bus='usb'>
                      <address type='usb' bus='0' port='1'/>
                    </input>
                    <input type='mouse' bus='ps2'/>
                    <input type='keyboard' bus='ps2'/>
                    <graphics type='vnc' port='-1' autoport='yes' listen='127.0.0.1'>
                      <listen type='address' address='127.0.0.1'/>
                    </graphics>
                    <video>
                      <model type='cirrus' vram='16384' heads='1' primary='yes'/>
                    </video>
                    <memballoon model='virtio'>
                      <address type='pci' domain='0x0000' bus='0x00' slot='0x08' function='0x0'/>
                    </memballoon>
                    <rng model='virtio'>
                      <backend model='random'>/dev/urandom</backend>
                    </rng>
                  </devices>
                </domain>
                """,
                request.getName(),
                request.getMemory(),
                request.getVcpu(),
                request.getName(),
                cloudInitIsoPath);
    }

    public Domain defineAndStartDomain(String domainXml) throws LibvirtException {
        Domain domain = libvirtConnect.domainDefineXML(domainXml);
        domain.create();
        return domain;
    }

    public List<Domain> getAllDomains() throws LibvirtException {
        return Arrays.stream(libvirtConnect.listDomains())
                .mapToObj(id -> {
                    try {
                        return libvirtConnect.domainLookupByID(id);
                    } catch (LibvirtException e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toList());
    }

    public Domain getDomainByName(String name) throws LibvirtException {
        return libvirtConnect.domainLookupByName(name);
    }

    public void deleteDomain(String name) throws LibvirtException {
        Domain domain = getDomainByName(name);
        if (domain.isActive() == 1) {
            domain.destroy();
        }
        domain.undefine();
    }

    public void startDomain(String name) throws LibvirtException {
        Domain domain = getDomainByName(name);
        domain.create();
    }

    public void shutdownDomain(String name) throws LibvirtException {
        Domain domain = getDomainByName(name);
        domain.shutdown();
    }

    public void waitForVmReady(String ip, int port, int timeoutSeconds) throws InterruptedException {
        int waited = 0;
        while (waited < timeoutSeconds) {
            try (Socket socket = new Socket()) {
                socket.connect(new InetSocketAddress(ip, port), 2000);
                System.out.println("SSH is ready!");
                return;
            } catch (IOException e) {
                Thread.sleep(1000);
                waited++;
            }
        }
        throw new RuntimeException("SSH not ready after " + timeoutSeconds + " seconds");
    }
}
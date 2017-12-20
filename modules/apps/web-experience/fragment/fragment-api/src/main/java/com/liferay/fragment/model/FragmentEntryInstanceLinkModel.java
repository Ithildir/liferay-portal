/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.fragment.model;

import aQute.bnd.annotation.ProviderType;

import com.liferay.expando.kernel.model.ExpandoBridge;

import com.liferay.portal.kernel.model.BaseModel;
import com.liferay.portal.kernel.model.CacheModel;
import com.liferay.portal.kernel.service.ServiceContext;

import java.io.Serializable;

/**
 * The base model interface for the FragmentEntryInstanceLink service. Represents a row in the &quot;FragmentEntryInstanceLink&quot; database table, with each column mapped to a property of this class.
 *
 * <p>
 * This interface and its corresponding implementation {@link com.liferay.fragment.model.impl.FragmentEntryInstanceLinkModelImpl} exist only as a container for the default property accessors generated by ServiceBuilder. Helper methods and all application logic should be put in {@link com.liferay.fragment.model.impl.FragmentEntryInstanceLinkImpl}.
 * </p>
 *
 * @author Brian Wing Shun Chan
 * @see FragmentEntryInstanceLink
 * @see com.liferay.fragment.model.impl.FragmentEntryInstanceLinkImpl
 * @see com.liferay.fragment.model.impl.FragmentEntryInstanceLinkModelImpl
 * @generated
 */
@ProviderType
public interface FragmentEntryInstanceLinkModel extends BaseModel<FragmentEntryInstanceLink> {
	/*
	 * NOTE FOR DEVELOPERS:
	 *
	 * Never modify or reference this interface directly. All methods that expect a fragment entry instance link model instance should use the {@link FragmentEntryInstanceLink} interface instead.
	 */

	/**
	 * Returns the primary key of this fragment entry instance link.
	 *
	 * @return the primary key of this fragment entry instance link
	 */
	public long getPrimaryKey();

	/**
	 * Sets the primary key of this fragment entry instance link.
	 *
	 * @param primaryKey the primary key of this fragment entry instance link
	 */
	public void setPrimaryKey(long primaryKey);

	/**
	 * Returns the fragment entry instance link ID of this fragment entry instance link.
	 *
	 * @return the fragment entry instance link ID of this fragment entry instance link
	 */
	public long getFragmentEntryInstanceLinkId();

	/**
	 * Sets the fragment entry instance link ID of this fragment entry instance link.
	 *
	 * @param fragmentEntryInstanceLinkId the fragment entry instance link ID of this fragment entry instance link
	 */
	public void setFragmentEntryInstanceLinkId(long fragmentEntryInstanceLinkId);

	/**
	 * Returns the group ID of this fragment entry instance link.
	 *
	 * @return the group ID of this fragment entry instance link
	 */
	public long getGroupId();

	/**
	 * Sets the group ID of this fragment entry instance link.
	 *
	 * @param groupId the group ID of this fragment entry instance link
	 */
	public void setGroupId(long groupId);

	/**
	 * Returns the fragment entry ID of this fragment entry instance link.
	 *
	 * @return the fragment entry ID of this fragment entry instance link
	 */
	public long getFragmentEntryId();

	/**
	 * Sets the fragment entry ID of this fragment entry instance link.
	 *
	 * @param fragmentEntryId the fragment entry ID of this fragment entry instance link
	 */
	public void setFragmentEntryId(long fragmentEntryId);

	/**
	 * Returns the layout page template entry ID of this fragment entry instance link.
	 *
	 * @return the layout page template entry ID of this fragment entry instance link
	 */
	public long getLayoutPageTemplateEntryId();

	/**
	 * Sets the layout page template entry ID of this fragment entry instance link.
	 *
	 * @param layoutPageTemplateEntryId the layout page template entry ID of this fragment entry instance link
	 */
	public void setLayoutPageTemplateEntryId(long layoutPageTemplateEntryId);

	/**
	 * Returns the position of this fragment entry instance link.
	 *
	 * @return the position of this fragment entry instance link
	 */
	public int getPosition();

	/**
	 * Sets the position of this fragment entry instance link.
	 *
	 * @param position the position of this fragment entry instance link
	 */
	public void setPosition(int position);

	@Override
	public boolean isNew();

	@Override
	public void setNew(boolean n);

	@Override
	public boolean isCachedModel();

	@Override
	public void setCachedModel(boolean cachedModel);

	@Override
	public boolean isEscapedModel();

	@Override
	public Serializable getPrimaryKeyObj();

	@Override
	public void setPrimaryKeyObj(Serializable primaryKeyObj);

	@Override
	public ExpandoBridge getExpandoBridge();

	@Override
	public void setExpandoBridgeAttributes(BaseModel<?> baseModel);

	@Override
	public void setExpandoBridgeAttributes(ExpandoBridge expandoBridge);

	@Override
	public void setExpandoBridgeAttributes(ServiceContext serviceContext);

	@Override
	public Object clone();

	@Override
	public int compareTo(FragmentEntryInstanceLink fragmentEntryInstanceLink);

	@Override
	public int hashCode();

	@Override
	public CacheModel<FragmentEntryInstanceLink> toCacheModel();

	@Override
	public FragmentEntryInstanceLink toEscapedModel();

	@Override
	public FragmentEntryInstanceLink toUnescapedModel();

	@Override
	public String toString();

	@Override
	public String toXmlString();
}
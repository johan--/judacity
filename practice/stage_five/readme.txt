XYZReader		= �������� ������

XYZReaderF		= �������� ViewPager

XYZReaderG		= ViewPager
					= TextView  load
					= bottom popup menu
					= Handler ProgressBar setup
					
					
��������.  ����� ����������� ��� Handler ��������� � Background �������� textView		


RViewPager		= ������� shared component
					=  https://guides.codepath.com/android/shared-element-activity-transition
				= �������� �������
					= ��������� �������� View ����� ��������� ��� ������� ������������� �������
					= ��������� ������� ����� transition_name
					= ������������ Intent ���������� ��� ������������ ����
				= ������ style Activity
				= ������  windowRequest(newExplode())
				= FAB �������� FrameLayout �� shared ������� ������ FAB	
					= ����� ����� Fab �� ������ ��� ������
					= ������� ������ ������� FAB ��� ������ � Collapsed
				
				
RViewPagerA		= ������ � getWindow() 	��������
RViewPagerB		= ������ � styles 		�����������

RViewPagerC		= Activity >> Activity >> Fragment transition ok
RViewPagerD		= ������ �������� ��������� � ������������� RecyclerView ViewPager
					= ����� ��� Activity to Fragment  
					= RecyclerView to ViewPager � �������
					= Animation after Transition
						= ��������. ����������� ��������� �� null ������ ��� ��� ���������� �� ����� � ������ Activity.
					= ����� ����� Adapter.hasStableId()
						=��������. ��� ����� ����� �� ������, ������������ ������ ��� ����������� ������ notifyDataChanged()
									������ RecyclerView ��� ���� ���� �������� ���������� ViewHolder,
									������������ ������ � ������� getItemId() ������� ���������� position.
									
RViewPagerE		= ������ RViewPagerD ���  callback ��� Glide
					= ��������� https://guides.codepath.com/android/shared-element-activity-transition
					= �������� ��� ������� ��� ������� ����������� 
					
RViewPagerF		= ������ RViewPagerD ����������� ��� Glide
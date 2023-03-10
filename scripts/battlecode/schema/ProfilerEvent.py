# automatically generated by the FlatBuffers compiler, do not modify

# namespace: schema

import flatbuffers
from flatbuffers.compat import import_numpy
np = import_numpy()

# These tables are set-up so that they match closely with speedscope's file format documented at
# https://github.com/jlfwong/speedscope/wiki/Importing-from-custom-sources.
# The client uses speedscope to show the recorded data in an interactive interface.
# A single event in a profile. Represents either an open event (meaning a
# method has been entered) or a close event (meaning the method was exited).
class ProfilerEvent(object):
    __slots__ = ['_tab']

    @classmethod
    def GetRootAs(cls, buf, offset=0):
        n = flatbuffers.encode.Get(flatbuffers.packer.uoffset, buf, offset)
        x = ProfilerEvent()
        x.Init(buf, n + offset)
        return x

    @classmethod
    def GetRootAsProfilerEvent(cls, buf, offset=0):
        """This method is deprecated. Please switch to GetRootAs."""
        return cls.GetRootAs(buf, offset)
    # ProfilerEvent
    def Init(self, buf, pos):
        self._tab = flatbuffers.table.Table(buf, pos)

    # Whether this is an open event (true) or a close event (false).
    # ProfilerEvent
    def IsOpen(self):
        o = flatbuffers.number_types.UOffsetTFlags.py_type(self._tab.Offset(4))
        if o != 0:
            return bool(self._tab.Get(flatbuffers.number_types.BoolFlags, o + self._tab.Pos))
        return False

    # The bytecode counter at the time the event occurred.
    # ProfilerEvent
    def At(self):
        o = flatbuffers.number_types.UOffsetTFlags.py_type(self._tab.Offset(6))
        if o != 0:
            return self._tab.Get(flatbuffers.number_types.Int32Flags, o + self._tab.Pos)
        return 0

    # The index of the method name in the ProfilerFile.frames array.
    # ProfilerEvent
    def Frame(self):
        o = flatbuffers.number_types.UOffsetTFlags.py_type(self._tab.Offset(8))
        if o != 0:
            return self._tab.Get(flatbuffers.number_types.Int32Flags, o + self._tab.Pos)
        return 0

def ProfilerEventStart(builder): builder.StartObject(3)
def Start(builder):
    return ProfilerEventStart(builder)
def ProfilerEventAddIsOpen(builder, isOpen): builder.PrependBoolSlot(0, isOpen, 0)
def AddIsOpen(builder, isOpen):
    return ProfilerEventAddIsOpen(builder, isOpen)
def ProfilerEventAddAt(builder, at): builder.PrependInt32Slot(1, at, 0)
def AddAt(builder, at):
    return ProfilerEventAddAt(builder, at)
def ProfilerEventAddFrame(builder, frame): builder.PrependInt32Slot(2, frame, 0)
def AddFrame(builder, frame):
    return ProfilerEventAddFrame(builder, frame)
def ProfilerEventEnd(builder): return builder.EndObject()
def End(builder):
    return ProfilerEventEnd(builder)